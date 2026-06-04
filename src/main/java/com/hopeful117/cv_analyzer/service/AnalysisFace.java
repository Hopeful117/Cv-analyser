package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AnalysisFace{
    private final PdfParserService pdfParserService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final JobScrapperService jobScrapperService;

    public ResumeAnalysis analyze(
            MultipartFile file,
            String jobOffer,
            String jobOfferUrl
    ) throws Exception {

        String resumeText =
                pdfParserService.extractText(file);

        String offerText =
                resolveOffer(jobOffer, jobOfferUrl);

        return resumeAnalysisService.analyzeResume(
                resumeText,
                offerText
        );
    }

    private String resolveOffer(
            String jobOffer,
            String jobOfferUrl
    ) throws IOException {

        if (jobOfferUrl != null
                && !jobOfferUrl.isBlank()) {

            return jobScrapperService
                    .extractTextFromUrl(jobOfferUrl);
        }

        return jobOffer;
    }
}


