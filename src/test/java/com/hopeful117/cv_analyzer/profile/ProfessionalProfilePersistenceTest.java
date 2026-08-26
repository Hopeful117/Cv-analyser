package com.hopeful117.cv_analyzer.profile;

import com.hopeful117.cv_analyzer.exception.InvalidProfileException;
import com.hopeful117.cv_analyzer.profile.application.ProfessionalProfileService;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProfileView;
import com.hopeful117.cv_analyzer.profile.domain.EducationKind;
import com.hopeful117.cv_analyzer.profile.domain.SkillOrigin;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository;
import com.hopeful117.cv_analyzer.profile.web.ProfileForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProfessionalProfilePersistenceTest {

    @Autowired ProfessionalProfileRepository repository;
    @Autowired ProfessionalProfileService service;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesProfileTables() {
        assertThat(tableExists("career_professional_profile")).isTrue();
        assertThat(tableExists("career_profile_skill")).isTrue();
        assertThat(tableExists("career_profile_experience")).isTrue();
        assertThat(tableExists("career_profile_education")).isTrue();
        assertThat(tableExists("career_profile_language")).isTrue();
    }

    @Test
    void persistsFullAggregateWithChildrenAndOrigins() {
        ProfileForm form = new ProfileForm();
        form.setFullName("Ludovic Martin");
        form.setProfessionalTitle("Développeur Java");
        form.setReferenceLocation("Lyon, France");
        form.setSkillsText("Java\nSpring Boot");
        form.setLanguagesText("Français : natif\nAnglais : courant");
        form.setEducationText("Master Informatique | Université de Lyon | 2019");
        form.setCertificationText("AWS Developer | Amazon | 2023");
        ProfileForm.ExperienceLine line = new ProfileForm.ExperienceLine();
        line.setTitle("Développeur");
        line.setCompany("ACME");
        line.setStartDate(LocalDate.of(2020, 1, 1));
        form.setExperiences(List.of(line));

        Long id = service.saveFromForm(form);
        repository.flush();

        ProfileView view = service.getProfileView().orElseThrow();
        assertThat(view.fullName()).isEqualTo("Ludovic Martin");
        assertThat(view.skills()).hasSize(2);
        assertThat(view.languages()).hasSize(2);
        assertThat(view.languages().get(0).level()).isEqualTo("natif");
        assertThat(view.educations()).hasSize(2);
        assertThat(view.experiences()).hasSize(1);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM career_profile_skill WHERE profile_id = ?",
                Integer.class, id)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM career_profile_education WHERE kind = ?",
                Integer.class, EducationKind.CERTIFICATION.name())).isEqualTo(1);
    }

    @Test
    void normalizedSkillUniquenessIsEnforcedAtDatabaseLevel() {
        ProfileForm form = new ProfileForm();
        form.setFullName("Test");
        form.setSkillsText("Spring Boot");
        Long id = service.saveFromForm(form);
        repository.flush();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO career_profile_skill (profile_id, item_order, label, normalized_name, origin) "
                        + "VALUES (?, 9, 'spring  boot', 'spring boot', 'MANUAL')",
                id))
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
    }

    @Test
    void manualEditReplacesCollectionsAndResetsCvProvenance() {
        ProfessionalProfileEntity existing = new ProfessionalProfileEntity();
        existing.setFullName("Ancien");
        var cvSkill = skillOf("Kubernetes", "kubernetes");
        cvSkill.setOrigin(SkillOrigin.FROM_CV);
        existing.addSkill(cvSkill);
        existing.setAiProvider("OpenAI");
        existing.setAiModel("gpt-4o-mini");
        existing.setPromptVersion("profile-extraction-v1");
        existing.setCvAssistedAt(java.time.Instant.now());
        repository.saveAndFlush(existing);

        ProfileForm form = new ProfileForm();
        form.setFullName("Nouveau");
        form.setSkillsText("Java");

        service.saveFromForm(form);
        repository.flush();

        ProfileView view = service.getProfileView().orElseThrow();
        assertThat(view.fullName()).isEqualTo("Nouveau");
        assertThat(view.skills()).extracting(ProfileViewModels.SkillView::label)
                .containsExactly("Java");
        assertThat(repository.findLocalProfile().orElseThrow().getCvAssistedAt()).isNull();

        List<String> origins = jdbcTemplate.queryForList(
                "SELECT origin FROM career_profile_skill", String.class);
        assertThat(origins).containsExactly(SkillOrigin.MANUAL.name());
    }

    @Test
    void invalidManualSubmissionLeavesExistingProfileIntact() {
        ProfileForm initial = new ProfileForm();
        initial.setSkillsText("Java");
        service.saveFromForm(initial);

        ProfileForm broken = new ProfileForm();
        broken.setSkillsText("Go\nGo");

        assertThatThrownBy(() -> service.saveFromForm(broken))
                .isInstanceOf(InvalidProfileException.class);

        assertThat(service.getProfileView().orElseThrow().skills())
                .extracting(ProfileViewModels.SkillView::label)
                .containsExactly("Java");
    }

    private static com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity skillOf(
            String label, String normalized) {
        var skill = new com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity();
        skill.setLabel(label);
        skill.setNormalizedName(normalized);
        return skill;
    }

    private boolean tableExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE upper(table_name) = ?",
                Integer.class, name.toUpperCase());
        return count != null && count > 0;
    }
}
