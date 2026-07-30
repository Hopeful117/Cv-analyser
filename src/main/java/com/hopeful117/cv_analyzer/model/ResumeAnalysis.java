package com.hopeful117.cv_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysis {
    private int overallScore;
    private int cvQualityScore;
    private int atsScore;
    private int jobMatchScore;
    private String jobOfferLanguage;
    private List<String> atsRisks;
    private List<String> recommendations;
    private List<String> missingKeywords;

    public ResumeAnalysis(
            int overallScore,
            int cvQualityScore,
            int atsScore,
            int jobMatchScore,
            List<String> atsRisks,
            List<String> recommendations,
            List<String> missingKeywords
    ) {
        this(
                overallScore,
                cvQualityScore,
                atsScore,
                jobMatchScore,
                "fr",
                atsRisks,
                recommendations,
                missingKeywords
        );
    }
}
