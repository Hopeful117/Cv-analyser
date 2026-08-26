package com.hopeful117.cv_analyzer.discovery.domain;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.TechnologyPreference;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceTechnologyEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEvaluatorTest {

    @Test
    void eligibleWhenNoConstraintsViolated() {
        JobOffer offer = createOffer(ContractType.CDI, WorkMode.HYBRID, null);
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    @Test
    void ineligibleWhenContractNotAccepted() {
        JobOffer offer = createOffer(ContractType.CDD, WorkMode.HYBRID, null);
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.CONTRACT_NOT_ACCEPTED);
    }

    @Test
    void reviewRequiredWhenContractUnknown() {
        JobOffer offer = createOffer(null, WorkMode.HYBRID, "UNKNOWN_CODE");
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.REVIEW_REQUIRED);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.CONTRACT_UNKNOWN);
    }

    @Test
    void reviewRequiredWhenWorkModeUnknown() {
        JobOffer offer = createOffer(ContractType.CDI, null, null);
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.REVIEW_REQUIRED);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.WORK_MODE_UNKNOWN);
    }

    @Test
    void ineligibleWhenWorkModeNotAccepted() {
        JobOffer offer = createOffer(ContractType.CDI, WorkMode.REMOTE, null);
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.ONSITE),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.WORK_MODE_NOT_ACCEPTED);
    }

    @Test
    void ineligibleWhenExcludedTechnologyFound() {
        JobOffer offer = createOfferWithCompetencies(
                ContractType.CDI,
                WorkMode.HYBRID,
                List.of(new JobOfferCompetency("1", "JavaScript", "S"))
        );
        JobSearchPreferencesEntity prefs = createPreferencesWithTechnologies(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                List.of(createTechnology("javascript", TechnologyPreference.EXCLUDED))
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.EXCLUDED_TECHNOLOGY_FOUND);
    }

    @Test
    void ineligibleWhenSalaryBelowMinimum() {
        JobOffer offer = createOfferWithSalary(
                ContractType.CDI,
                WorkMode.HYBRID,
                new BigDecimal("40000"),
                SalaryPeriod.ANNUAL
        );
        JobSearchPreferencesEntity prefs = createPreferencesWithSalary(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                50000,
                SalaryPeriod.ANNUAL
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.INELIGIBLE);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.SALARY_BELOW_MINIMUM);
    }

    @Test
    void reviewRequiredWhenSalaryUnknown() {
        JobOffer offer = createOfferWithSalary(
                ContractType.CDI,
                WorkMode.HYBRID,
                null,
                null
        );
        JobSearchPreferencesEntity prefs = createPreferencesWithSalary(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                50000,
                SalaryPeriod.ANNUAL
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.REVIEW_REQUIRED);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.SALARY_UNKNOWN);
    }

    @Test
    void preferredTechnologyProducesPositiveEvidence() {
        JobOffer offer = createOfferWithCompetencies(
                ContractType.CDI,
                WorkMode.HYBRID,
                List.of(new JobOfferCompetency("1", "Java", "S"))
        );
        JobSearchPreferencesEntity prefs = createPreferencesWithTechnologies(
                Set.of(ContractType.CDI),
                Set.of(WorkMode.HYBRID),
                List.of(createTechnology("java", TechnologyPreference.PREFERRED))
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.ELIGIBLE);
        assertThat(result.reasons()).anyMatch(r -> r.type() == EligibilityReasonType.PREFERRED_TECHNOLOGY_FOUND);
    }

    @Test
    void noRejectionWhenPreferencesEmpty() {
        JobOffer offer = createOffer(ContractType.CDI, WorkMode.HYBRID, null);
        JobSearchPreferencesEntity prefs = createPreferences(
                Set.of(),
                Set.of(),
                null,
                null
        );

        EligibilityResult result = EligibilityEvaluator.evaluate(offer, prefs);

        assertThat(result.status()).isEqualTo(EligibilityStatus.ELIGIBLE);
    }

    private JobOffer createOffer(ContractType contractType, WorkMode workMode, String rawContractCode) {
        return new JobOffer(
                "france-travail",
                "12345",
                "https://example.com",
                java.time.Instant.now(),
                "Developer Java",
                "Description",
                "Company",
                "M1855",
                "Développeur web",
                "Développeur back-end",
                "75 - Paris",
                "75056",
                "75001",
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                contractType,
                rawContractCode,
                contractType != null ? contractType.name() : rawContractCode,
                List.of(),
                "3 ans",
                workMode,
                "35H/semaine",
                "Annuel de 50000 Euros sur 12 mois",
                new BigDecimal("50000"),
                new BigDecimal("50000"),
                SalaryPeriod.ANNUAL,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }

    private JobOffer createOfferWithCompetencies(ContractType contractType, WorkMode workMode,
                                                  List<JobOfferCompetency> competencies) {
        return new JobOffer(
                "france-travail",
                "12345",
                "https://example.com",
                java.time.Instant.now(),
                "Developer Java",
                "Description",
                "Company",
                "M1855",
                "Développeur web",
                "Développeur back-end",
                "75 - Paris",
                "75056",
                "75001",
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                contractType,
                "CDI",
                "CDI",
                competencies,
                "3 ans",
                workMode,
                "35H/semaine",
                "Annuel de 50000 Euros sur 12 mois",
                new BigDecimal("50000"),
                new BigDecimal("50000"),
                SalaryPeriod.ANNUAL,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }

    private JobOffer createOfferWithSalary(ContractType contractType, WorkMode workMode,
                                            BigDecimal salaryMin, SalaryPeriod salaryPeriod) {
        return new JobOffer(
                "france-travail",
                "12345",
                "https://example.com",
                java.time.Instant.now(),
                "Developer Java",
                "Description",
                "Company",
                "M1855",
                "Développeur web",
                "Développeur back-end",
                "75 - Paris",
                "75056",
                "75001",
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                contractType,
                "CDI",
                "CDI",
                List.of(),
                "3 ans",
                workMode,
                "35H/semaine",
                salaryMin != null ? "Annuel de " + salaryMin + " Euros sur 12 mois" : null,
                salaryMin,
                salaryMin,
                salaryPeriod,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }

    private JobSearchPreferencesEntity createPreferences(
            Set<ContractType> contractTypes,
            Set<WorkMode> workModes,
            Integer salaryMin,
            SalaryPeriod salaryPeriod
    ) {
        JobSearchPreferencesEntity prefs = new JobSearchPreferencesEntity();
        prefs.setContractTypes(contractTypes);
        prefs.setAcceptedWorkModes(workModes);
        prefs.setSalaryMinAmount(salaryMin);
        prefs.setSalaryPeriod(salaryPeriod);
        return prefs;
    }

    private JobSearchPreferencesEntity createPreferencesWithTechnologies(
            Set<ContractType> contractTypes,
            Set<WorkMode> workModes,
            List<PreferenceTechnologyEntity> technologies
    ) {
        JobSearchPreferencesEntity prefs = createPreferences(contractTypes, workModes, null, null);
        prefs.setTechnologies(technologies);
        return prefs;
    }

    private JobSearchPreferencesEntity createPreferencesWithSalary(
            Set<ContractType> contractTypes,
            Set<WorkMode> workModes,
            Integer salaryMin,
            SalaryPeriod salaryPeriod
    ) {
        return createPreferences(contractTypes, workModes, salaryMin, salaryPeriod);
    }

    private PreferenceTechnologyEntity createTechnology(String name, TechnologyPreference kind) {
        PreferenceTechnologyEntity tech = new PreferenceTechnologyEntity();
        tech.setNormalizedName(name);
        tech.setLabel(name);
        tech.setKind(kind);
        return tech;
    }
}
