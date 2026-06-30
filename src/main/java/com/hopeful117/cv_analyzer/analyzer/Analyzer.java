package com.hopeful117.cv_analyzer.analyzer;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;

public interface Analyzer {
    ResumeAnalysis analyze(String resumeText, String jobOfferText);
}
