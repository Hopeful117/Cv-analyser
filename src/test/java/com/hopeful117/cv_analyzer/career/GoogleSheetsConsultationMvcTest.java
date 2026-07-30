package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.WebInterfaceController.GoogleSheetsConsultationController;
import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionService;
import com.hopeful117.cv_analyzer.career.application.consultation.*;
import com.hopeful117.cv_analyzer.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GoogleSheetsConsultationMvcTest {
    private ConsultGoogleSheetApplicationsUseCase consultation;
    private ImportGoogleSheetApplicationUseCase importer;
    private ApplicationProjectionService projection;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        consultation = mock(ConsultGoogleSheetApplicationsUseCase.class);
        importer = mock(ImportGoogleSheetApplicationUseCase.class);
        projection = mock(ApplicationProjectionService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new GoogleSheetsConsultationController(consultation, importer, projection))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void displaysPaginatedConsultation() throws Exception {
        when(consultation.consult(null, null, null, "title", "asc", 0, 25))
                .thenReturn(new GoogleSheetConsultationView(Page.empty(), 0,
                        0, 0, 0, 0, 0, 0, Instant.now()));

        mockMvc.perform(get("/google-sheets/applications"))
                .andExpect(status().isOk())
                .andExpect(view().name("google-sheets-applications"))
                .andExpect(model().attributeExists("consultation", "states"));
    }

    @Test
    void displaysDetailAndResynchronizesTheResolvedCrmApplication() throws Exception {
        var result = new GoogleSheetComparisonResult(
                GoogleSheetComparisonState.SYNCHRONIZED, null, null, 42L, List.of());
        var detail = new GoogleSheetApplicationDetailView(
                "APPLICATION-42", result, List.of(), Map.of());
        when(consultation.details("APPLICATION-42")).thenReturn(detail);

        mockMvc.perform(get("/google-sheets/applications/APPLICATION-42"))
                .andExpect(status().isOk())
                .andExpect(view().name("google-sheets-application-detail"))
                .andExpect(model().attribute("detail", detail));

        mockMvc.perform(post("/google-sheets/applications/APPLICATION-42/sync"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/google-sheets/applications/APPLICATION-42"));
        verify(projection).synchronize(42L);
    }
}
