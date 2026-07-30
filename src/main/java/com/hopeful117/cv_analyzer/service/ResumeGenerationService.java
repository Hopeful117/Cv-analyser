package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.generator.AiResumeGenerator;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeGenerationService {
    private final AiResumeGenerator resumeGenerator;

    public GeneratedResume generateCorrectedResume(
            String resumeText,
            String jobOfferText,
            ResumeAnalysis analysis
    ) {
        return resumeGenerator.generate(resumeText, jobOfferText, analysis);
    }
}
