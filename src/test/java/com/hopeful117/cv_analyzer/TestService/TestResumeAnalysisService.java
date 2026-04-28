package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TestResumeAnalysisService {

    @Mock
    private ResumeAnalyzer resumeAnalyzer;

    private ResumeAnalysisService resumeAnalysisService;

    @BeforeEach
    void setUp() {
        resumeAnalysisService = new ResumeAnalysisService(resumeAnalyzer);
    }

    @Test
    void shouldDelegateToAnalyzer() {
        // Test d'intégration simple : le service délègue bien à l'analyzer
        String resumeText = "Java developer";
        String jobOfferText = "Java Spring developer";

        ResumeAnalysis expectedResult = new ResumeAnalysis(75, 80, 100, 50,
            List.of(), List.of(), List.of("spring"));

        Mockito.when(resumeAnalyzer.analyze(resumeText, jobOfferText)).thenReturn(expectedResult);

        ResumeAnalysis result = resumeAnalysisService.analyzeResume(resumeText, jobOfferText);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        Mockito.verify(resumeAnalyzer).analyze(resumeText, jobOfferText);
    }

    @Test
    void shouldHandleNullInputs() {
        // Test de robustesse : gestion des entrées nulles
        Mockito.when(resumeAnalyzer.analyze(null, null)).thenThrow(new IllegalArgumentException("Invalid input"));

        assertThrows(IllegalArgumentException.class, () ->
            resumeAnalysisService.analyzeResume(null, null));
    }
}
