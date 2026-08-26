package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FranceTravailOfferMapperTest {

    @Test
    void mapsBasicOfferFields() {
        FranceTravailOfferDto dto = createDto(
                "12345",
                "Développeur Java",
                "CDI",
                "CDI",
                "75 - Paris",
                "Company"
        );

        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.providerOfferId()).isEqualTo("12345");
        assertThat(offer.title()).isEqualTo("Développeur Java");
        assertThat(offer.canonicalContractType()).isEqualTo(ContractType.CDI);
        assertThat(offer.rawContractCode()).isEqualTo("CDI");
        assertThat(offer.rawContractLabel()).isEqualTo("CDI");
        assertThat(offer.locationLabel()).isEqualTo("75 - Paris");
        assertThat(offer.company()).isEqualTo("Company");
        assertThat(offer.providerKey()).isEqualTo("france-travail");
    }

    @Test
    void mapsNullDtoToNull() {
        assertThat(FranceTravailOfferMapper.toDomain(null)).isNull();
    }

    @Test
    void mapsCdiContract() {
        FranceTravailOfferDto dto = createDto("1", "Title", "CDI", "CDI", null, null);
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);
        assertThat(offer.canonicalContractType()).isEqualTo(ContractType.CDI);
    }

    @Test
    void mapsCddContract() {
        FranceTravailOfferDto dto = createDto("1", "Title", "CDD", "CDD", null, null);
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);
        assertThat(offer.canonicalContractType()).isEqualTo(ContractType.CDD);
    }

    @Test
    void mapsInterimContract() {
        FranceTravailOfferDto dto = createDto("1", "Title", "INTERIM", "Intérim", null, null);
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);
        assertThat(offer.canonicalContractType()).isEqualTo(com.hopeful117.cv_analyzer.career.domain.ContractType.INTERIM);
    }

    @Test
    void mapsUnknownContractToNull() {
        FranceTravailOfferDto dto = createDto("1", "Title", "AUTRE", "Autre", null, null);
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);
        assertThat(offer.canonicalContractType()).isNull();
    }

    @Test
    void parsesSingleSalary() {
        FranceTravailOfferDto dto = createDtoWithSalary("Annuel de 65000.0 Euros sur 12.0 mois");
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.salaryMinAmount()).isEqualTo(new BigDecimal("65000.0"));
        assertThat(offer.salaryMaxAmount()).isEqualTo(new BigDecimal("65000.0"));
        assertThat(offer.salaryPeriod()).isEqualTo(SalaryPeriod.ANNUAL);
    }

    @Test
    void parsesRangeSalary() {
        FranceTravailOfferDto dto = createDtoWithSalary("Annuel de 40000.0 Euros à 50000.0 Euros sur 12.0 mois");
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.salaryMinAmount()).isEqualTo(new BigDecimal("40000.0"));
        assertThat(offer.salaryMaxAmount()).isEqualTo(new BigDecimal("50000.0"));
        assertThat(offer.salaryPeriod()).isEqualTo(SalaryPeriod.ANNUAL);
    }

    @Test
    void handlesNullSalary() {
        FranceTravailOfferDto dto = createDtoWithSalary(null);
        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.rawSalaryText()).isNull();
        assertThat(offer.salaryMinAmount()).isNull();
        assertThat(offer.salaryMaxAmount()).isNull();
        assertThat(offer.salaryPeriod()).isNull();
    }

    @Test
    void mapsCompetencies() {
        FranceTravailOfferDto.FranceTravailCompetencyDto comp1 =
                new FranceTravailOfferDto.FranceTravailCompetencyDto("300688", "Application web", "S");
        FranceTravailOfferDto.FranceTravailCompetencyDto comp2 =
                new FranceTravailOfferDto.FranceTravailCompetencyDto("109846", "Java", "E");

        FranceTravailOfferDto dto = new FranceTravailOfferDto(
                "1", "Title", "Desc", null, null, null, null, null, null,
                null, "CDI", "CDI", null, "E", "3 ans",
                List.of(comp1, comp2), null, null, null, null, null, null, null
        );

        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.competencies()).hasSize(2);
        assertThat(offer.competencies().get(0).label()).isEqualTo("Application web");
        assertThat(offer.competencies().get(0).requirement()).isEqualTo("S");
        assertThat(offer.competencies().get(1).label()).isEqualTo("Java");
        assertThat(offer.competencies().get(1).requirement()).isEqualTo("E");
    }

    @Test
    void mapsLocation() {
        FranceTravailOfferDto.FranceTravailLocationDto location =
                new FranceTravailOfferDto.FranceTravailLocationDto(
                        "92 - CLICHY",
                        BigDecimal.valueOf(48.902793),
                        BigDecimal.valueOf(2.30484),
                        "92110",
                        "92024"
                );

        FranceTravailOfferDto dto = new FranceTravailOfferDto(
                "1", "Title", "Desc", null, null, location, null, null, null,
                null, "CDI", "CDI", null, "E", "3 ans",
                List.of(), null, null, null, null, null, null, null
        );

        JobOffer offer = FranceTravailOfferMapper.toDomain(dto);

        assertThat(offer.locationLabel()).isEqualTo("92 - CLICHY");
        assertThat(offer.postalCode()).isEqualTo("92110");
        assertThat(offer.communeCode()).isEqualTo("92024");
        assertThat(offer.latitude()).isEqualTo(BigDecimal.valueOf(48.902793));
        assertThat(offer.longitude()).isEqualTo(BigDecimal.valueOf(2.30484));
    }

    private FranceTravailOfferDto createDto(String id, String title, String typeContrat,
                                             String typeContratLibelle, String location, String company) {
        FranceTravailOfferDto.FranceTravailLocationDto loc = location != null
                ? new FranceTravailOfferDto.FranceTravailLocationDto(location, null, null, null, null)
                : null;
        FranceTravailOfferDto.FranceTravailCompanyDto ent = company != null
                ? new FranceTravailOfferDto.FranceTravailCompanyDto(company)
                : null;

        return new FranceTravailOfferDto(
                id, title, "Description", null, null, loc, null, null, null,
                ent, typeContrat, typeContratLibelle, null, "E", "3 ans",
                List.of(), null, null, null, null, null, null, null
        );
    }

    private FranceTravailOfferDto createDtoWithSalary(String salaryLibelle) {
        FranceTravailOfferDto.FranceTravailSalaryDto salary = salaryLibelle != null
                ? new FranceTravailOfferDto.FranceTravailSalaryDto(salaryLibelle, null, null)
                : null;

        return new FranceTravailOfferDto(
                "1", "Title", "Desc", null, null, null, null, null, null,
                null, "CDI", "CDI", null, "E", "3 ans",
                List.of(), salary, null, null, null, null, null, null
        );
    }
}
