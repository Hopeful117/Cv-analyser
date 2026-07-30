package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.generator.AiResumeGenerator;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.ResumeGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestResumeGenerationService {

    @Mock
    private AiResumeGenerator resumeGenerator;

    private ResumeGenerationService resumeGenerationService;

    @BeforeEach
    void setUp() {
        resumeGenerationService = new ResumeGenerationService(resumeGenerator);
    }

    @Test
    void shouldGenerateResumeFromSourceOfferAndAnalysis() {
        String resumeText = "Développeur Java chez Exemple";
        String offerText = "Recherche développeur Java et Docker";
        ResumeAnalysis analysis = new ResumeAnalysis(
                70, 80, 90, 40,
                List.of(),
                List.of("Clarifier les expériences"),
                List.of("Docker")
        );
        GeneratedResume expected = new GeneratedResume(
                "DÉVELOPPEUR JAVA\nExemple\n[À COMPLÉTER : dates de l'expérience]",
                List.of("[À COMPLÉTER : dates de l'expérience]"),
                List.of("Structure clarifiée")
        );

        when(resumeGenerator.generate(resumeText, offerText, analysis))
                .thenReturn(expected);

        GeneratedResume result = resumeGenerationService.generateCorrectedResume(
                resumeText,
                offerText,
                analysis
        );

        assertThat(result).isEqualTo(expected);
        verify(resumeGenerator).generate(resumeText, offerText, analysis);
    }
}
