package com.hopeful117.cv_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResumeAnalysis {
    private int overallScore;
    private int cvQualityScore;
    private int atsScore;
    private int jobMatchScore;
    private List<String> atsRisks;
    private List<String> recommendations;
    private List<String> missingKeywords;




}
