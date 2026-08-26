package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchRequest;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityEvaluator;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoverJobOffers {

    private final JobOfferProvider provider;
    private final ProfessionalProfileRepository profileRepository;
    private final JobSearchPreferencesRepository preferencesRepository;

    public DiscoveryResult discover(String targetRole) {
        if (!provider.isAvailable()) {
            return DiscoveryResult.providerUnavailable();
        }

        var profile = profileRepository.findLocalProfile();
        if (profile.isEmpty()) {
            return DiscoveryResult.profileMissing();
        }

        var preferences = preferencesRepository.findActivePreferences();
        if (preferences.isEmpty()) {
            return DiscoveryResult.preferencesMissing();
        }

        try {
            JobOfferSearchRequest request = JobOfferSearchRequest.of(targetRole);
            JobOfferSearchResult searchResult = provider.search(request);

            List<EligibleOffer> eligibleOffers = searchResult.offers().stream()
                    .map(offer -> new EligibleOffer(offer, EligibilityEvaluator.evaluate(offer, preferences.get())))
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

    public record EligibleOffer(JobOffer offer, EligibilityResult eligibility) {
    }
}
