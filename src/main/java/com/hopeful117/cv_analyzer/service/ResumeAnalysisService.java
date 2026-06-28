package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.analyzer.AiResumeAnalyzer;
import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {
    private final AiResumeAnalyzer resumeAnalyzer;

    public ResumeAnalysis analyzeResume(String resumeText, String jobOfferText) {
        return resumeAnalyzer.analyze(resumeText, jobOfferText);
    }
}
