package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.helper.ResolveOffer;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.model.ResumeAnalysisResult;
import com.hopeful117.cv_analyzer.service.AnalysisFace;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import com.hopeful117.cv_analyzer.service.ResumeGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestAnalysisFace {

    @Mock
    private PdfParserService pdfParserService;
    @Mock
    private ResumeAnalysisService resumeAnalysisService;
    @Mock
    private ResumeGenerationService resumeGenerationService;
    @Mock
    private ResolveOffer resolveOffer;
    @Mock
    private MultipartFile file;

    private AnalysisFace analysisFace;

    @BeforeEach
    void setUp() {
        analysisFace = new AnalysisFace(
                pdfParserService,
                resumeAnalysisService,
                resumeGenerationService,
                resolveOffer
        );
    }

    @Test
    void shouldGenerateCorrectedResumeAfterAnalysis() throws Exception {
        String resumeText = "CV source";
        String offerText = "Offre résolue";
        ResumeAnalysis analysis = new ResumeAnalysis(
                75, 80, 90, 55,
                "en",
                List.of("Risque ATS"),
                List.of("Recommandation"),
                List.of("Compétence absente")
        );
        GeneratedResume generatedResume = new GeneratedResume(
                "CV corrigé\n[À COMPLÉTER : information manquante]",
                List.of("[À COMPLÉTER : information manquante]"),
                List.of("Correction")
        );

        when(pdfParserService.extractText(file)).thenReturn(resumeText);
        when(resolveOffer.resolveOffer("offre", null)).thenReturn(offerText);
        when(resumeAnalysisService.analyzeResume(resumeText, offerText)).thenReturn(analysis);
        when(resumeGenerationService.generateCorrectedResume(resumeText, offerText, analysis))
                .thenReturn(generatedResume);

        ResumeAnalysisResult result = analysisFace.analyze(file, "offre", null);

        assertThat(result.analysis()).isSameAs(analysis);
        assertThat(result.analysis().getJobOfferLanguage()).isEqualTo("en");
        assertThat(result.generatedResume()).isSameAs(generatedResume);
        var order = inOrder(resumeAnalysisService, resumeGenerationService);
        order.verify(resumeAnalysisService).analyzeResume(resumeText, offerText);
        order.verify(resumeGenerationService)
                .generateCorrectedResume(resumeText, offerText, analysis);
    }
}
