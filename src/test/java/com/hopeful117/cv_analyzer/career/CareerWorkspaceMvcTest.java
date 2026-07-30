package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.WebInterfaceController.AnalyzerController;
import com.hopeful117.cv_analyzer.WebInterfaceController.CareerWorkspaceController;
import com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService;
import com.hopeful117.cv_analyzer.config.GlobalExceptionHandler;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CareerWorkspaceMvcTest {
    private CareerWorkspaceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(CareerWorkspaceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AnalyzerController(service),
                        new CareerWorkspaceController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postAnalysisUsesPostRedirectGet() throws Exception {
        when(service.analyze(any(), eq("Offre"), isNull(), eq("Java"), eq("ACME")))
                .thenReturn(42L);
        MockMultipartFile cv = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "pdf".getBytes());

        mockMvc.perform(multipart("/analyze")
                        .file(cv)
                        .param("jobOffer", "Offre")
                        .param("opportunityTitle", "Java")
                        .param("companyName", "ACME"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/analyses/42"));
    }

    @Test
    void unknownAnalysisReturnsProduct404() throws Exception {
        when(service.getAnalysis(99L)).thenThrow(new EntityNotFoundException("Analyse introuvable."));
        mockMvc.perform(get("/analyses/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 404));
    }
}
