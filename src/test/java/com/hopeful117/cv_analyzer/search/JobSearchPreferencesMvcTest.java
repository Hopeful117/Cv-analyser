package com.hopeful117.cv_analyzer.search;

import com.hopeful117.cv_analyzer.WebInterfaceController.JobSearchPreferencesController;
import com.hopeful117.cv_analyzer.config.GlobalExceptionHandler;
import com.hopeful117.cv_analyzer.exception.InvalidSearchPreferencesException;
import com.hopeful117.cv_analyzer.search.application.JobSearchPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class JobSearchPreferencesMvcTest {

    private JobSearchPreferencesService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(JobSearchPreferencesService.class);
        View neutralView = new View() {
            @Override
            public void render(Map<String, ?> model, HttpServletRequest request,
                               HttpServletResponse response) {
            }

            @Override
            public String getContentType() {
                return "text/html";
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new JobSearchPreferencesController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setViewResolvers((viewName, locale) -> viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : neutralView)
                .build();
    }

    @Test
    void emptyStateWhenNoPreferences() throws Exception {
        when(service.getPreferencesView()).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/preferences"))
                .andExpect(status().isOk())
                .andExpect(view().name("preferences"))
                .andExpect(model().attributeDoesNotExist("preferences"));
    }

    @Test
    void createUsesPostRedirectGetWithSuccessMessage() throws Exception {
        mockMvc.perform(post("/preferences")
                        .param("targetRolesText", "Développeur Java"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/preferences"))
                .andExpect(flash().attribute("successMessage",
                        "Vos préférences de recherche ont été enregistrées."));
    }

    @Test
    void editRedirectsToCreationWhenNothingDefined() throws Exception {
        when(service.getPreferencesView()).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/preferences/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/preferences/new"));
    }

    @Test
    void oversizedFieldReRendersForm() throws Exception {
        mockMvc.perform(post("/preferences")
                        .param("targetRolesText", "x".repeat(5_001)))
                .andExpect(status().isOk())
                .andExpect(view().name("preferences-form"));
    }

    @Test
    void domainConflictSurfacesProductErrorWithoutSilentSave() throws Exception {
        Mockito.doThrow(new InvalidSearchPreferencesException(
                        "Des technologies sont à la fois recherchées et exclues : Java."))
                .when(service).saveFromForm(any());

        mockMvc.perform(post("/preferences")
                        .param("preferredTechnologiesText", "Java")
                        .param("excludedTechnologiesText", "java"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));

        verify(service).saveFromForm(any());
    }

    /**
     * Garde-fou UX (régression réelle constatée sur le profil) : la sidebar doit exposer
     * exactement un lien fonctionnel vers la recherche et ne plus contenir aucun
     * placeholder « bientôt ».
     */
    @Test
    void navigationHasSingleFunctionalSearchEntryAndNoPlaceholder() throws IOException {
        Path sidebar = Path.of("src/main/resources/templates/fragments/sidebar.html");
        String html = Files.readString(sidebar);

        assertThat(html).contains("@{/preferences}");
        assertThat(countOccurrences(html, "@{/preferences}")).isEqualTo(1);
        assertThat(html).doesNotContain("nav-soon");
        assertThat(html).doesNotContain("Bientôt");
        assertThat(html).contains("Ma recherche");
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int index = 0;
        while ((index = haystack.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
