package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoverJobOffersTest {

    @Mock
    private JobOfferProvider provider;
    @Mock
    private ProfessionalProfileRepository profileRepository;
    @Mock
    private JobSearchPreferencesRepository preferencesRepository;

    @Test
    void calculatesAndRanksMatchingWithoutChangingEligibility() {
        ProfessionalProfileEntity profile = new ProfessionalProfileEntity();
        profile.setProfessionalTitle("Développeur Java");
        JobSearchPreferencesEntity preferences = new JobSearchPreferencesEntity();
        JobOffer weak = offer("Comptable");
        JobOffer strong = offer("Développeur Java");
        when(provider.isAvailable()).thenReturn(true);
        when(profileRepository.findLocalProfile()).thenReturn(Optional.of(profile));
        when(preferencesRepository.findActivePreferences()).thenReturn(Optional.of(preferences));
        when(provider.search(org.mockito.ArgumentMatchers.any())).thenReturn(
                new JobOfferSearchResult(List.of(weak, strong), 2, 2, "Développeur Java", "provider", null));

        DiscoverJobOffers.DiscoveryResult result = new DiscoverJobOffers(
                provider, profileRepository, preferencesRepository).discover("Développeur Java");

        assertThat(result.offers()).hasSize(2);
        assertThat(result.offers().getFirst().offer().title()).isEqualTo("Développeur Java");
        assertThat(result.offers().getFirst().matching().score())
                .isGreaterThan(result.offers().get(1).matching().score());
        assertThat(result.offers()).allMatch(item -> item.eligibility() != null);
    }

    @Test
    void equalScoresKeepProviderOrder() {
        ProfessionalProfileEntity profile = new ProfessionalProfileEntity();
        JobSearchPreferencesEntity preferences = new JobSearchPreferencesEntity();
        JobOffer first = offer("Unknown first");
        JobOffer second = offer("Unknown second");
        when(provider.isAvailable()).thenReturn(true);
        when(profileRepository.findLocalProfile()).thenReturn(Optional.of(profile));
        when(preferencesRepository.findActivePreferences()).thenReturn(Optional.of(preferences));
        when(provider.search(org.mockito.ArgumentMatchers.any())).thenReturn(
                new JobOfferSearchResult(List.of(first, second), 2, 2, "Role", "provider", null));

        DiscoverJobOffers.DiscoveryResult result = new DiscoverJobOffers(
                provider, profileRepository, preferencesRepository).discover("Role");

        assertThat(result.offers()).extracting(item -> item.offer().title())
                .containsExactly("Unknown first", "Unknown second");
        assertThat(result.offers()).extracting(item -> item.matching().score())
                .containsExactly(50, 50);
    }

    private JobOffer offer(String title) {
        return new JobOffer(
                "provider", title, "https://example.com", null, title, null, "Company",
                null, null, null, "Paris", null, null, null, null, null, null, null,
                List.of(), null, null, null, null, null, null, null, null, null
        );
    }
}
