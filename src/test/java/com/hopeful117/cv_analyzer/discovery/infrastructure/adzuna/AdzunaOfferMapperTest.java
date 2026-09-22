package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AdzunaOfferMapperTest {

    @Test
    void mapsExplicitFieldsAndPreservesUnknowns() {
        AdzunaSearchResponse.AdzunaOfferDto dto = new AdzunaSearchResponse.AdzunaOfferDto(
                "adz-1", "Java Developer", "Backend role", "https://adzuna.example/adz-1",
                "2026-09-22T10:15:00Z", new AdzunaSearchResponse.AdzunaLocation("Paris"),
                BigDecimal.valueOf(48.85), BigDecimal.valueOf(2.35),
                BigDecimal.valueOf(45000), BigDecimal.valueOf(55000), "permanent", "full_time",
                new AdzunaSearchResponse.AdzunaCompany("Example"),
                new AdzunaSearchResponse.AdzunaCategory("IT Jobs", "it-jobs"));

        JobOffer offer = AdzunaOfferMapper.toDomain(dto);

        assertThat(offer.providerKey()).isEqualTo("adzuna");
        assertThat(offer.providerOfferId()).isEqualTo("adz-1");
        assertThat(offer.title()).isEqualTo("Java Developer");
        assertThat(offer.company()).isEqualTo("Example");
        assertThat(offer.locationLabel()).isEqualTo("Paris");
        assertThat(offer.canonicalContractType()).isEqualTo(ContractType.CDI);
        assertThat(offer.rawContractCode()).isEqualTo("permanent");
        assertThat(offer.salaryMinAmount()).isEqualTo(BigDecimal.valueOf(45000));
        assertThat(offer.salaryMaxAmount()).isEqualTo(BigDecimal.valueOf(55000));
        assertThat(offer.salaryPeriod()).isNull();
        assertThat(offer.workMode()).isNull();
        assertThat(offer.providerCreatedAt()).isNotNull();
    }

    @Test
    void unknownContractDoesNotBecomeAffirmativeCanonicalValue() {
        AdzunaSearchResponse.AdzunaOfferDto dto = new AdzunaSearchResponse.AdzunaOfferDto(
                "adz-2", "Role", null, null, null, null, null, null, null, null,
                "unknown", null, null, null);

        assertThat(AdzunaOfferMapper.toDomain(dto).canonicalContractType()).isNull();
    }
}
