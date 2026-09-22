package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchRequest;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobMatchResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class DiscoverJobOffers {

    private final JobOfferProvider provider;
    private final EvaluateJobOffer evaluateJobOffer;

    @Autowired
    public DiscoverJobOffers(JobOfferProvider provider, EvaluateJobOffer evaluateJobOffer) {
        this.provider = provider;
        this.evaluateJobOffer = evaluateJobOffer;
    }

    public DiscoverJobOffers(JobOfferProvider provider,
                             com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository profileRepository,
                             com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository preferencesRepository) {
        this(provider, new EvaluateJobOffer(profileRepository, preferencesRepository));
    }

    @Transactional(readOnly = true)
    public DiscoveryResult discover(String targetRole) {
        if (!provider.isAvailable()) {
            return DiscoveryResult.providerUnavailable();
        }
        String readinessError = evaluateJobOffer.readinessError();
        if (readinessError != null) {
            return readinessError.contains("profil")
                    ? DiscoveryResult.profileMissing()
                    : DiscoveryResult.preferencesMissing();
        }

        try {
            JobOfferSearchRequest request = JobOfferSearchRequest.of(targetRole);
            JobOfferSearchResult searchResult = provider.search(request);

            List<EligibleOffer> eligibleOffers = searchResult.offers().stream()
                    .map(offer -> {
                        EvaluateJobOffer.EvaluationResult evaluation = evaluateJobOffer.evaluate(offer);
                        if (!evaluation.success()) {
                            throw new IllegalStateException(evaluation.errorMessage());
                        }
                        return new EligibleOffer(offer, evaluation.eligibility(), evaluation.matching());
                    })
                    .sorted(Comparator.comparingInt((EligibleOffer item) -> item.matching().score()).reversed())
                    .toList();

            return DiscoveryResult.success(
                    eligibleOffers,
                    searchResult.returnedCount(),
                    searchResult.totalAvailable(),
                    searchResult.targetRole(),
                    searchResult.providerKey()
            );
        } catch (Exception e) {
            log.error("Error searching France Travail: {}", e.getMessage(), e);
            return DiscoveryResult.error(e.getMessage());
        }
    }

    public record DiscoveryResult(
            boolean success,
            List<EligibleOffer> offers,
            int returnedCount,
            int totalAvailable,
            String targetRole,
            String providerKey,
            String errorMessage
    ) {
        public static DiscoveryResult success(List<EligibleOffer> offers, int returnedCount,
                                              int totalAvailable, String targetRole, String providerKey) {
            return new DiscoveryResult(true, offers, returnedCount, totalAvailable, targetRole, providerKey, null);
        }

        public static DiscoveryResult providerUnavailable() {
            return new DiscoveryResult(false, List.of(), 0, 0, null, null,
                    "L'intégration France Travail n'est pas configurée.");
        }

        public static DiscoveryResult profileMissing() {
            return new DiscoveryResult(false, List.of(), 0, 0, null, null,
                    "Votre profil professionnel n'est pas encore créé.");
        }

        public static DiscoveryResult preferencesMissing() {
            return new DiscoveryResult(false, List.of(), 0, 0, null, null,
                    "Vos préférences de recherche ne sont pas encore configurées.");
        }

        public static DiscoveryResult error(String message) {
            return new DiscoveryResult(false, List.of(), 0, 0, null, null, message);
        }
    }

    public record EligibleOffer(JobOffer offer, EligibilityResult eligibility, JobMatchResult matching) {
        public EligibleOffer(JobOffer offer, EligibilityResult eligibility) {
            this(offer, eligibility, JobMatchResult.unknown("Matching non calculé dans ce contexte."));
        }
    }
}
