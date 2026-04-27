package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import org.springframework.stereotype.Service;

@Service
public class ResumeAnalysisService {
    private final ResumeAnalyzer resumeAnalyzer = new ResumeAnalyzer();

    public ResumeAnalysis analyzeResume(String resumeText, String jobOfferText) {
        return resumeAnalyzer.analyze(resumeText, jobOfferText);
    }
}
