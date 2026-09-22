package com.hopeful117.cv_analyzer.discovery.domain;

import com.hopeful117.cv_analyzer.profile.persistence.ProfileExperienceEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileEntity;
import com.hopeful117.cv_analyzer.search.domain.TechnologyPreference;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceRoleEntity;
import com.hopeful117.cv_analyzer.search.persistence.PreferenceTechnologyEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobMatchingEngineTest {

    @Test
    void strongMatchUsesRoleSkillsAndPreferredTechnologies() {
        ProfessionalProfileEntity profile = profile("Développeur Java backend", "Java", "Spring Boot");
        JobSearchPreferencesEntity preferences = preferences("Développeur Java", "Java");
        JobOffer offer = offer("Développeur Java backend", "Java", "Spring Boot");

        JobMatchResult result = JobMatchingEngine.evaluate(profile, preferences, offer);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.positiveSignals()).extracting(MatchSignal::message)
                .anyMatch(message -> message.contains("Java"));
        assertThat(result.gaps()).isEmpty();
    }

    @Test
    void partialMatchReportsMissingSkillsWithoutMakingOfferIneligible() {
        ProfessionalProfileEntity profile = profile("Développeur Java", "Java");
        JobSearchPreferencesEntity preferences = preferences("Développeur Java", null);
        JobOffer offer = offer("Développeur Java backend", "Java", "Spring Boot");

        JobMatchResult result = JobMatchingEngine.evaluate(profile, preferences, offer);

        assertThat(result.score()).isBetween(50, 90);
        assertThat(result.gaps()).extracting(MatchSignal::message)
                .anyMatch(message -> message.contains("Spring Boot"));
    }

    @Test
    void weakMatchHasLowScoreAndExplicitGaps() {
        ProfessionalProfileEntity profile = profile("Comptable", "Excel");
        JobSearchPreferencesEntity preferences = preferences("Comptable", null);
        JobOffer offer = offer("Développeur Java", "Java", "Spring Boot");

        JobMatchResult result = JobMatchingEngine.evaluate(profile, preferences, offer);

        assertThat(result.score()).isEqualTo(0);
        assertThat(result.gaps()).hasSize(3);
    }

    @Test
    void missingInformationIsUnknownAndDoesNotCreateAFalseGap() {
        JobMatchResult result = JobMatchingEngine.evaluate(
                new ProfessionalProfileEntity(), new JobSearchPreferencesEntity(), offer("Développeur Java"));

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.gaps()).isEmpty();
        assertThat(result.unknowns()).isNotEmpty();
    }

    @Test
    void excludedTechnologyDoesNotBecomeASecondMatchingPenalty() {
        ProfessionalProfileEntity profile = profile("Développeur Java", "Java");
        JobSearchPreferencesEntity preferences = preferences("Développeur Java", null);
        PreferenceTechnologyEntity excluded = technology("Java", TechnologyPreference.EXCLUDED);
        preferences.addTechnology(excluded);
        JobOffer offer = offer("Développeur Java", "Java");

        JobMatchResult result = JobMatchingEngine.evaluate(profile, preferences, offer);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.gaps()).isEmpty();
    }

    @Test
    void sameInputProducesSameBoundedResult() {
        ProfessionalProfileEntity profile = profile("Développeur Java", "Java");
        JobSearchPreferencesEntity preferences = preferences("Développeur Java", "Java");
        JobOffer offer = offer("Développeur Java", "Java");

        JobMatchResult first = JobMatchingEngine.evaluate(profile, preferences, offer);
        JobMatchResult second = JobMatchingEngine.evaluate(profile, preferences, offer);

        assertThat(first).isEqualTo(second);
        assertThat(first.score()).isBetween(0, 100);
    }

    @Test
    void matchingDoesNotDependOnProviderIdentity() {
        ProfessionalProfileEntity profile = profile("Développeur Java", "Java");
        JobSearchPreferencesEntity preferences = preferences("Développeur Java", "Java");
        JobOffer providerOffer = offer("Développeur Java", "Java");
        JobOffer manualOffer = new JobOffer(
                "manual", providerOffer.providerOfferId(), providerOffer.originUrl(), providerOffer.fetchedAt(),
                providerOffer.title(), providerOffer.description(), providerOffer.company(), providerOffer.romeCode(),
                providerOffer.romeLabel(), providerOffer.appellationLabel(), providerOffer.locationLabel(),
                providerOffer.communeCode(), providerOffer.postalCode(), providerOffer.latitude(), providerOffer.longitude(),
                providerOffer.canonicalContractType(), providerOffer.rawContractCode(), providerOffer.rawContractLabel(),
                providerOffer.competencies(), providerOffer.experienceLabel(), providerOffer.workMode(),
                providerOffer.workDurationLabel(), providerOffer.rawSalaryText(), providerOffer.salaryMinAmount(),
                providerOffer.salaryMaxAmount(), providerOffer.salaryPeriod(), providerOffer.providerCreatedAt(),
                providerOffer.providerUpdatedAt());

        assertThat(JobMatchingEngine.evaluate(profile, preferences, manualOffer))
                .isEqualTo(JobMatchingEngine.evaluate(profile, preferences, providerOffer));
    }

    @Test
    void duplicateOfferSkillsCountOnceAndKeepTheStrongestRequirement() {
        ProfessionalProfileEntity profile = profile("Développeur Java", "Java");
        JobOffer offer = new JobOffer(
                "provider", "id", "https://example.com", null, "Développeur Java", null, "Company",
                null, null, null, "Paris", null, null, null, null, null, null, null,
                List.of(new JobOfferCompetency("1", "Java", "S"),
                        new JobOfferCompetency("2", "java", "E"),
                        new JobOfferCompetency("3", "Spring Boot", "E")),
                null, null, null, null, null, null, null, null, null
        );

        JobMatchResult result = JobMatchingEngine.evaluate(profile, new JobSearchPreferencesEntity(), offer);

        assertThat(result.positiveSignals()).filteredOn(signal -> signal.type() == MatchSignalType.SKILL)
                .hasSize(1);
        assertThat(result.gaps()).filteredOn(signal -> signal.type() == MatchSignalType.SKILL)
                .singleElement().extracting(MatchSignal::message).asString().contains("Spring Boot");
    }

    @Test
    void oneEvaluableRoleDimensionIsScoredAndMissingDimensionsRemainExplicitlyUnknown() {
        ProfessionalProfileEntity profile = profile("Développeur Java");

        JobMatchResult result = JobMatchingEngine.evaluate(
                profile, new JobSearchPreferencesEntity(), offer("Développeur Java"));

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.unknowns()).hasSize(1);
        assertThat(result.unknowns().getFirst().message()).contains("compétences");
    }

    private ProfessionalProfileEntity profile(String title, String... skills) {
        ProfessionalProfileEntity profile = new ProfessionalProfileEntity();
        profile.setProfessionalTitle(title);
        for (String skill : skills) {
            ProfileSkillEntity entity = new ProfileSkillEntity();
            entity.setLabel(skill);
            entity.setNormalizedName(skill.toLowerCase());
            profile.addSkill(entity);
        }
        ProfileExperienceEntity experience = new ProfileExperienceEntity();
        experience.setTitle(title);
        profile.addExperience(experience);
        return profile;
    }

    private JobSearchPreferencesEntity preferences(String role, String preferredTechnology) {
        JobSearchPreferencesEntity preferences = new JobSearchPreferencesEntity();
        if (role != null) {
            PreferenceRoleEntity roleEntity = new PreferenceRoleEntity();
            roleEntity.setLabel(role);
            roleEntity.setNormalizedLabel(role.toLowerCase());
            preferences.addTargetRole(roleEntity);
        }
        if (preferredTechnology != null) {
            preferences.addTechnology(technology(preferredTechnology, TechnologyPreference.PREFERRED));
        }
        return preferences;
    }

    private PreferenceTechnologyEntity technology(String label, TechnologyPreference kind) {
        PreferenceTechnologyEntity technology = new PreferenceTechnologyEntity();
        technology.setLabel(label);
        technology.setNormalizedName(label.toLowerCase());
        technology.setKind(kind);
        return technology;
    }

    private JobOffer offer(String title, String... competencies) {
        return new JobOffer(
                "provider", "id", "https://example.com", null, title, null, "Company",
                null, null, null, "Paris", null, null, null, null, null, null, null,
                java.util.Arrays.stream(competencies)
                        .map(value -> new JobOfferCompetency(value, value, "E"))
                        .toList(),
                null, null, null, null, null, null, null, null, null
        );
    }
}
