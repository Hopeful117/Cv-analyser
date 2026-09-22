package com.hopeful117.cv_analyzer.discovery.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobOfferIdentityTest {

    @Test
    void exposesProviderLocalIdentityWithoutClaimingOriginalSource() {
        JobOffer offer = new JobOffer(
                "adzuna", "adz-123", "https://adzuna.example/jobs/adz-123", null,
                "Backend Engineer", null, "Example", null, null, null, null, null, null,
                null, null, null, null, null, List.of(), null, null, null, null,
                null, null, null, null, null
        );

        assertThat(offer.providerIdentity())
                .isEqualTo(new JobOfferIdentity("adzuna", "adz-123"));
        assertThat(offer.originUrl()).isEqualTo("https://adzuna.example/jobs/adz-123");
    }
}
