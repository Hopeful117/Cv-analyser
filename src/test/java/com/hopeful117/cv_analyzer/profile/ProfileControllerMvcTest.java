package com.hopeful117.cv_analyzer.profile;

import com.hopeful117.cv_analyzer.WebInterfaceController.ProfileController;
import com.hopeful117.cv_analyzer.config.GlobalExceptionHandler;
import com.hopeful117.cv_analyzer.exception.AIAnalysisException;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProposalReview;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProfileView;
import com.hopeful117.cv_analyzer.profile.application.ProfessionalProfileService;
import com.hopeful117.cv_analyzer.profile.web.ProfileForm;
import com.hopeful117.cv_analyzer.profile.web.ProfileProposalForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ProfileControllerMvcTest {

    private ProfessionalProfileService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(ProfessionalProfileService.class);
        View neutralView = new View() {
            @Override
            public void render(Map<String, ?> model, HttpServletRequest request,
                               HttpServletResponse response) {
                // rendu neutre : on teste les noms de vues et les redirections
            }

            @Override
            public String getContentType() {
                return "text/html";
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new ProfileController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setViewResolvers((viewName, locale) -> {
                    if (viewName.startsWith("redirect:")) {
                        return new org.springframework.web.servlet.view.RedirectView(
                                viewName.substring("redirect:".length()));
                    }
                    return neutralView;
                })
                .build();
    }

    @Test
    void emptyProfileShowsEmptyState() throws Exception {
        when(service.getProfileView()).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeDoesNotExist("profile"));
    }

    @Test
    void existingProfileIsRendered() throws Exception {
        when(service.getProfileView()).thenReturn(Optional.of(new ProfileView(
                1L, "Ludovic", "Développeur", "Lyon",
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of(), false, java.time.Instant.now())));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    void manualCreationUsesPostRedirectGetWithSuccessMessage() throws Exception {
        mockMvc.perform(post("/profile")
                        .param("fullName", "Ludovic Martin")
                        .param("skillsText", "Java\nSpring Boot"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("successMessage",
                        "Votre profil professionnel a été créé."));
    }

    @Test
    void oversizedFieldReRendersManualCreationForm() throws Exception {
        mockMvc.perform(post("/profile")
                        .param("fullName", "x".repeat(201)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-form"));
    }

    @Test
    void editRedirectsToCreationWhenNoProfileExists() throws Exception {
        when(service.getProfileView()).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/new"));
    }

    @Test
    void initializeRendersEditableProposalWithoutPersisting() throws Exception {
        ProfileProposalForm form = new ProfileProposalForm();
        form.setApplyFullName(true);
        form.setFullName("Ludovic Martin");
        when(service.proposeFromCv(any())).thenReturn(new ProposalReview(form, "OpenAI", "gpt-4o-mini"));

        MockMultipartFile cv = new MockMultipartFile(
                "cvFile", "cv.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/profile/initialize").file(cv))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-proposal"))
                .andExpect(model().attributeExists("proposalForm"));

        verify(service, Mockito.never()).applyProposal(any());
    }

    @Test
    void extractionFailureReturnsProductErrorWithoutProfileLoss() throws Exception {
        when(service.proposeFromCv(any()))
                .thenThrow(new AIAnalysisException("IA indisponible", new RuntimeException()));

        MockMultipartFile cv = new MockMultipartFile(
                "cvFile", "cv.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/profile/initialize").file(cv))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("retryUrl", "/profile"));

        verify(service, Mockito.never()).saveFromForm(any(ProfileForm.class));
        verify(service, Mockito.never()).applyProposal(any());
    }

    @Test
    void applyProposalValidatesSelectionAndRedirectsWithSuccess() throws Exception {
        mockMvc.perform(post("/profile/apply")
                        .param("applyFullName", "true")
                        .param("fullName", "Ludovic Martin")
                        .param("aiProvider", "OpenAI")
                        .param("aiModel", "gpt-4o-mini"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(service).applyProposal(Mockito.argThat(form ->
                form.isApplyFullName() && "Ludovic Martin".equals(form.getFullName())));
    }

    @Test
    void invalidApplySubmissionReRendersProposalScreen() throws Exception {
        mockMvc.perform(post("/profile/apply")
                        .param("skills[0].label", "x".repeat(300)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-proposal"));

        assertThat(true).isTrue();
    }
}
