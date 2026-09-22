package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.WebInterfaceController.CareerWorkspaceController;
import com.hopeful117.cv_analyzer.career.application.OpportunityWorkspaceService;
import com.hopeful117.cv_analyzer.career.application.OpportunityWorkspaceViewModels;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.config.GlobalExceptionHandler;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OpportunityWorkspaceMvcTest {
    private OpportunityWorkspaceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(OpportunityWorkspaceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CareerWorkspaceController(Mockito.mock(com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService.class), service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setViewResolvers((viewName, locale) -> viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : new MappingJackson2JsonView())
                .build();
    }

    @Test
    void listsOpportunitiesAndExposesWorkspaceNavigationData() throws Exception {
        var item = new OpportunityWorkspaceViewModels.OpportunityListItem(
                7L, "Développeur Java", "ACME", OpportunityStatus.ANALYZED,
                Instant.parse("2026-09-22T10:00:00Z"), 1, 1, 1, 1);
        when(service.list()).thenReturn(List.of(item));

        mockMvc.perform(get("/opportunities"))
                .andExpect(status().isOk())
                .andExpect(view().name("opportunities"))
                .andExpect(model().attribute("opportunities", List.of(item)));
    }

    @Test
    void rendersWorkspaceWithAllArtifactCollections() throws Exception {
        var details = new OpportunityWorkspaceViewModels.OpportunityDetails(
                7L, "Développeur Java", "ACME", "Paris", ContractType.CDI,
                "CDI", RemoteMode.HYBRID, "france-travail", "https://example.test/job",
                "Description", OpportunityStatus.ANALYZED, Instant.now());
        var workspace = new OpportunityWorkspaceViewModels.Workspace(
                details,
                List.of(new OpportunityWorkspaceViewModels.AnalysisItem(11L, Instant.now(), 82, 79)),
                List.of(new OpportunityWorkspaceViewModels.ResumeItem(12L, 11L, "Backend Java", 2, Instant.now())),
                List.of(new OpportunityWorkspaceViewModels.LetterItem(13L, CoverLetterOrigin.AI_GENERATED, Instant.now())),
                List.of(new com.hopeful117.cv_analyzer.career.application.CrmViewModels.ApplicationListItem(
                        14L, "ACME", "Développeur Java", ContractType.CDI, "CDI", null,
                        ApplicationStatus.APPLIED, ApplicationPriority.HIGH, null, true, true,
                        ProjectionStatus.SYNCHRONIZED)));
        when(service.get(7L)).thenReturn(workspace);

        mockMvc.perform(get("/opportunities/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("opportunity-workspace"))
                .andExpect(model().attribute("workspace", workspace));
    }

    @Test
    void missingOpportunityUsesProduct404() throws Exception {
        when(service.get(99L)).thenThrow(new EntityNotFoundException("Opportunité introuvable."));

        mockMvc.perform(get("/opportunities/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 404));
    }
}
