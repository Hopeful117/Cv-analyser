package com.hopeful117.cv_analyzer.discovery.web;

import com.hopeful117.cv_analyzer.discovery.application.DiscoverJobOffers;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityResult;
import com.hopeful117.cv_analyzer.discovery.domain.EligibilityStatus;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobDiscoveryViewModelsTest {

    @Test
    void descriptionWithScriptTagIsStripped() {
        JobOffer offer = offerWithDescription("<script>alert('xss')</script>Dev Java backend");
        EligibilityResult eligibility = EligibilityResult.eligible();

        DiscoverJobOffers.DiscoveryResult result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, eligibility)),
                1, 1, "Dev Java", "france-travail");

        JobDiscoveryViewModels.ResultsViewModel vm = JobDiscoveryViewModels.toResults(result);
        String snippet = vm.offers().get(0).descriptionSnippet();

        assertThat(snippet).doesNotContain("<script>");
        assertThat(snippet).doesNotContain("</script>");
        assertThat(snippet).contains("Dev Java backend");
    }

    @Test
    void descriptionWithImageOnerrorIsStripped() {
        JobOffer offer = offerWithDescription("<img src=x onerror=alert(1)>Développeur Java");
        EligibilityResult eligibility = EligibilityResult.eligible();

        DiscoverJobOffers.DiscoveryResult result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, eligibility)),
                1, 1, "Dev Java", "france-travail");

        JobDiscoveryViewModels.ResultsViewModel vm = JobDiscoveryViewModels.toResults(result);
        String snippet = vm.offers().get(0).descriptionSnippet();

        assertThat(snippet).doesNotContain("<img");
        assertThat(snippet).doesNotContain("onerror");
        assertThat(snippet).contains("Développeur Java");
    }

    @Test
    void descriptionWithHtmlTagsIsCleaned() {
        JobOffer offer = offerWithDescription("<p>Java <b>Senior</b></p><br/>Backend");
        EligibilityResult eligibility = EligibilityResult.eligible();

        DiscoverJobOffers.DiscoveryResult result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, eligibility)),
                1, 1, "Dev Java", "france-travail");

        JobDiscoveryViewModels.ResultsViewModel vm = JobDiscoveryViewModels.toResults(result);
        String snippet = vm.offers().get(0).descriptionSnippet();

        assertThat(snippet).doesNotContain("<p>");
        assertThat(snippet).doesNotContain("<b>");
        assertThat(snippet).doesNotContain("<br");
        assertThat(snippet).contains("Java Senior");
        assertThat(snippet).contains("Backend");
    }

    @Test
    void nullDescriptionProducesNullSnippet() {
        JobOffer offer = offerWithDescription(null);
        EligibilityResult eligibility = EligibilityResult.eligible();

        DiscoverJobOffers.DiscoveryResult result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, eligibility)),
                1, 1, "Dev Java", "france-travail");

        JobDiscoveryViewModels.ResultsViewModel vm = JobDiscoveryViewModels.toResults(result);

        assertThat(vm.offers().get(0).descriptionSnippet()).isNull();
    }

    @Test
    void externalLinksUsePlainText() {
        JobOffer offer = offerWithDescription("Voir <a href=\"https://evil.com\">ici</a>");
        EligibilityResult eligibility = EligibilityResult.eligible();

        DiscoverJobOffers.DiscoveryResult result = DiscoverJobOffers.DiscoveryResult.success(
                List.of(new DiscoverJobOffers.EligibleOffer(offer, eligibility)),
                1, 1, "Dev Java", "france-travail");

        JobDiscoveryViewModels.ResultsViewModel vm = JobDiscoveryViewModels.toResults(result);
        String snippet = vm.offers().get(0).descriptionSnippet();

        assertThat(snippet).doesNotContain("<a");
        assertThat(snippet).contains("Voir ici");
    }

    private JobOffer offerWithDescription(String description) {
        return new JobOffer(
                "france-travail", "12345", "https://example.com",
                java.time.Instant.now(), "Developer Java", description, "Company",
                "M1855", "Dev web", "Dev back", "75 - Paris", "75056", "75001",
                BigDecimal.valueOf(48.85), BigDecimal.valueOf(2.35),
                ContractType.CDI, "CDI", "CDI",
                List.of(), "3 ans", WorkMode.HYBRID, "35H",
                "Annuel de 50000 Euros sur 12 mois",
                new BigDecimal("50000"), new BigDecimal("50000"),
                SalaryPeriod.ANNUAL,
                java.time.Instant.now(), java.time.Instant.now()
        );
    }
}
