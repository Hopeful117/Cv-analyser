package com.hopeful117.cv_analyzer.profile;

import com.hopeful117.cv_analyzer.career.application.UploadValidationService;
import com.hopeful117.cv_analyzer.exception.InvalidProfileException;
import com.hopeful117.cv_analyzer.profile.ai.AiProfileExtractor;
import com.hopeful117.cv_analyzer.profile.ai.ExtractedProfileProposal;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProposalReview;
import com.hopeful117.cv_analyzer.profile.application.ProfessionalProfileService;
import com.hopeful117.cv_analyzer.profile.domain.SkillOrigin;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileEntity;
import com.hopeful117.cv_analyzer.profile.persistence.ProfessionalProfileRepository;
import com.hopeful117.cv_analyzer.profile.web.ProfileForm;
import com.hopeful117.cv_analyzer.profile.web.ProfileProposalForm;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfessionalProfileServiceTest {

    private ProfessionalProfileRepository repository;
    private AiProfileExtractor extractor;
    private PdfParserService pdfParserService;
    private ProfessionalProfileService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ProfessionalProfileRepository.class);
        extractor = Mockito.mock(AiProfileExtractor.class);
        pdfParserService = Mockito.mock(PdfParserService.class);
        service = new ProfessionalProfileService(repository, extractor,
                pdfParserService, new UploadValidationService());
        ReflectionTestUtils.setField(service, "aiModel", "gpt-4o-mini");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ProfileForm manualForm(String skillsText) {
        ProfileForm form = new ProfileForm();
        form.setFullName("Ludovic Martin");
        form.setProfessionalTitle("Développeur Java");
        form.setSkillsText(skillsText);
        return form;
    }

    @Test
    void createsThenUpdatesSameLocalProfile() {
        when(repository.findLocalProfile()).thenReturn(Optional.empty())
                .thenReturn(Optional.of(new ProfessionalProfileEntity()));

        service.saveFromForm(manualForm("Java"));
        service.saveFromForm(manualForm("Java\nSpring Boot"));

        verify(repository, Mockito.times(2)).save(any());
    }

    @Test
    void normalizesAndRejectsDuplicateSkillsWithinManualSubmission() {
        assertThatThrownBy(() -> service.saveFromForm(manualForm("Spring Boot\nspring  boot")))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("plusieurs fois");
        verify(repository, never()).save(any());
    }

    @Test
    void normalizedSkillNamesIgnoreCaseAccentsAndPunctuationVariants() {
        assertThat(com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer.normalize("Café Java"))
                .isEqualTo("cafe java");
        assertThat(com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer.normalize("  C++   "))
                .isEqualTo("c++");
        assertThat(com.hopeful117.cv_analyzer.profile.domain.ProfileNormalizer.normalize(".NET Core"))
                .isEqualTo(".net core");
    }

    @Test
    void rejectsExperienceWithEndBeforeStart() {
        ProfileForm form = manualForm("Java");
        ProfileForm.ExperienceLine line = new ProfileForm.ExperienceLine();
        line.setTitle("Développeur");
        line.setStartDate(LocalDate.of(2024, 1, 1));
        line.setEndDate(LocalDate.of(2020, 1, 1));
        form.setExperiences(List.of(line));

        assertThatThrownBy(() -> service.saveFromForm(form))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("précède");
    }

    @Test
    void ignoresBlankExperienceRows() {
        ProfileForm form = manualForm("Java");
        form.setExperiences(List.of(new ProfileForm.ExperienceLine()));

        service.saveFromForm(form);

        verify(repository).save(Mockito.argThat(profile -> profile.getExperiences().isEmpty()));
    }

    @Test
    void proposalReviewIsEditableAndNeverPersistedDirectly() throws IOException {
        ExtractedProfileProposal proposal = new ExtractedProfileProposal();
        proposal.setFullName("Ludovic Martin");
        ExtractedProfileProposal.ProposedSkill skill = new ExtractedProfileProposal.ProposedSkill();
        skill.setName("Kubernetes");
        proposal.setSkills(List.of(skill));
        when(pdfParserService.extractText(any())).thenReturn("CV texte");
        when(extractor.extract(any())).thenReturn(proposal);

        ProposalReview review = service.proposeFromCv(cvFile());

        assertThat(review.form().isApplyFullName()).isTrue();
        assertThat(review.form().getSkills()).hasSize(1);
        assertThat(review.aiModel()).isEqualTo("gpt-4o-mini");
        verify(repository, never()).save(any());
    }

    @Test
    void applyProposalAddsItemsWithoutDuplicatingExistingOnes() {
        ProfessionalProfileEntity existing = new ProfessionalProfileEntity();
        existing.setFullName("Ancien Nom");
        existing.addSkill(skillOf("Java", "java", SkillOrigin.MANUAL));
        when(repository.findLocalProfile()).thenReturn(Optional.of(existing));

        ProfileProposalForm form = new ProfileProposalForm();
        form.setApplyFullName(true);
        form.setFullName("Nouveau Nom");
        ProfileProposalForm.SkillEntry java = new ProfileProposalForm.SkillEntry();
        java.setApply(true);
        java.setLabel("JAVA");
        ProfileProposalForm.SkillEntry k8s = new ProfileProposalForm.SkillEntry();
        k8s.setApply(true);
        k8s.setLabel("Kubernetes");
        form.setSkills(List.of(java, k8s));

        service.applyProposal(form);

        verify(repository).save(Mockito.argThat(profile ->
                "Nouveau Nom".equals(profile.getFullName())
                        && profile.getSkills().size() == 2
                        && profile.getSkills().stream()
                        .filter(s -> "Kubernetes".equals(s.getLabel()))
                        .allMatch(s -> s.getOrigin() == SkillOrigin.FROM_CV)
                        && profile.getPromptVersion().equals("profile-extraction-v1")));
    }

    @Test
    void applyProposalWithoutSelectionOnEmptyInstallationFailsWithoutSaving() {
        when(repository.findLocalProfile()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyProposal(new ProfileProposalForm()))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("au moins une donnée");
        verify(repository, never()).save(any());
    }

    @Test
    void applyProposalRetainingNothingLeavesProfileUntouched() {
        ProfessionalProfileEntity existing = new ProfessionalProfileEntity();
        existing.setFullName("Nom");
        when(repository.findLocalProfile()).thenReturn(Optional.of(existing));

        ProfileProposalForm form = new ProfileProposalForm();
        form.setApplyFullName(true);
        form.setFullName("   ");

        assertThatThrownBy(() -> service.applyProposal(form))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("inchangé");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectedEntriesNeverReachTheProfile() {
        ProfessionalProfileEntity existing = new ProfessionalProfileEntity();
        when(repository.findLocalProfile()).thenReturn(Optional.of(existing));

        ProfileProposalForm form = new ProfileProposalForm();
        ProfileProposalForm.SkillEntry rejected = new ProfileProposalForm.SkillEntry();
        rejected.setApply(false);
        rejected.setLabel("COBOL");
        form.setSkills(List.of(rejected));

        // Rien de coché et rien d'appliqué : la validation échoue sans aucune écriture.
        assertThatThrownBy(() -> service.applyProposal(form))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("inchangé");
        verify(repository, never()).save(any());
    }

    @Test
    void emptyManualSubmissionIsRejectedWithoutSaving() {
        assertThatThrownBy(() -> service.saveFromForm(new ProfileForm()))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("au moins une donnée");
        verify(repository, never()).save(any());
    }

    @Test
    void proposalCannotMutateRepositoryBeforeValidation() throws IOException {
        when(pdfParserService.extractText(any())).thenReturn("CV texte");
        when(extractor.extract(any())).thenReturn(new ExtractedProfileProposal());

        service.proposeFromCv(cvFile());

        verify(repository, never()).save(any());
    }

    private static MockMultipartFile cvFile() {
        return new MockMultipartFile("cvFile", "cv.pdf", "application/pdf", "pdf".getBytes());
    }

    private static com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity skillOf(
            String label, String normalized, SkillOrigin origin) {
        var skill = new com.hopeful117.cv_analyzer.profile.persistence.ProfileSkillEntity();
        skill.setLabel(label);
        skill.setNormalizedName(normalized);
        skill.setOrigin(origin);
        return skill;
    }
}
