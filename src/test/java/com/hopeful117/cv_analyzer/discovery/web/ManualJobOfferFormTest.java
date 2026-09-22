package com.hopeful117.cv_analyzer.discovery.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualJobOfferFormTest {

    @Test
    void mapsExternalOfferToCanonicalTransientOffer() {
        ManualJobOfferForm form = new ManualJobOfferForm();
        form.setTitle("Développeur Java");
        form.setCompany("Example");
        form.setOriginUrl("https://example.com/jobs/java");
        form.setDescription("Construire des services backend.");
        form.setCompetencies("Java, Spring Boot, Java");
        form.setRawContractCode("CDI");

        var offer = form.toJobOffer();

        assertThat(offer.providerKey()).isEqualTo("manual");
        assertThat(offer.providerOfferId()).startsWith("manual-");
        assertThat(offer.description()).isEqualTo("Construire des services backend.");
        assertThat(offer.canonicalContractType()).hasToString("CDI");
        assertThat(offer.competencies()).extracting(competency -> competency.label())
                .containsExactly("Java", "Spring Boot");
    }
}
