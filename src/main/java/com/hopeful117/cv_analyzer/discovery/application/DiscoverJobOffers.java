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
import java.util.stream.Stream;

@Slf4j
@Service
public class DiscoverJobOffers {

    private final List<JobOfferProvider> providers;
    private final EvaluateJobOffer evaluateJobOffer;

    @Autowired
    public DiscoverJobOffers(List<JobOfferProvider> providers, EvaluateJobOffer evaluateJobOffer) {
        this.providers = providers;
        this.evaluateJobOffer = evaluateJobOffer;
    }

    public DiscoverJobOffers(JobOfferProvider provider,
                             com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository profileRepository,
                             com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository preferencesRepository) {
        this(List.of(provider), new EvaluateJobOffer(profileRepository, preferencesRepository));
    }

    public DiscoverJobOffers(List<JobOfferProvider> providers,
                             com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository profileRepository,
                             com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository preferencesRepository) {
        this(providers, new EvaluateJobOffer(profileRepository, preferencesRepository));
    }

    @Transactional(readOnly = true)
    public DiscoveryResult discover(String targetRole) {
        List<JobOfferProvider> availableProviders = providers.stream()
                .filter(JobOfferProvider::isAvailable)
                .toList();
        if (availableProviders.isEmpty()) {
            return DiscoveryResult.providersUnavailable();
        }
        String readinessError = evaluateJobOffer.readinessError();
        if (readinessError != null) {
            return readinessError.contains("profil")
                    ? DiscoveryResult.profileMissing()
                    : DiscoveryResult.preferencesMissing();
        }

        JobOfferSearchRequest request = JobOfferSearchRequest.of(targetRole);
        List<JobOfferSearchResult> results = availableProviders.stream()
                .flatMap(provider -> search(provider, request))
                .toList();

        if (results.isEmpty()) {
            return DiscoveryResult.error("Aucun fournisseur d'offres n'a pu répondre à la recherche.");
        }

        try {
            List<EligibleOffer> eligibleOffers = results.stream()
                    .flatMap(result -> result.offers().stream())
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
                    results.stream().mapToInt(JobOfferSearchResult::returnedCount).sum(),
                    results.stream().mapToInt(JobOfferSearchResult::totalAvailable).sum(),
                    targetRole,
                    results.size() == 1 ? results.getFirst().providerKey() : "multiple"
            );
        } catch (Exception e) {
            log.error("Error evaluating discovered job offers: {}", e.getMessage(), e);
            return DiscoveryResult.error(e.getMessage());
        }
    }

    private Stream<JobOfferSearchResult> search(JobOfferProvider provider, JobOfferSearchRequest request) {
        try {
            return Stream.of(provider.search(request));
        } catch (Exception e) {
            log.error("Error searching provider {}: {}", provider.getClass().getSimpleName(), e.getMessage(), e);
            return Stream.empty();
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

        public static DiscoveryResult providersUnavailable() {
            return new DiscoveryResult(false, List.of(), 0, 0, null, null,
                    "Aucune intégration d'offres n'est configurée.");
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
