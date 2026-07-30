package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.helper.ResolveOffer;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.model.ResumeAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnalysisFace{
    private final PdfParserService pdfParserService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeGenerationService resumeGenerationService;
    private final ResolveOffer resolveOffer;

    public ResumeAnalysisResult analyze(
            MultipartFile file,
            String jobOffer,
            String jobOfferUrl
    ) throws Exception {

        String resumeText =
                pdfParserService.extractText(file);

        String offerText =
                resolveOffer.resolveOffer(jobOffer, jobOfferUrl);

        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(
                resumeText,
                offerText
        );

        GeneratedResume generatedResume =
                resumeGenerationService.generateCorrectedResume(
                        resumeText,
                        offerText,
                        analysis
                );

        return new ResumeAnalysisResult(analysis, generatedResume);
    }


}

