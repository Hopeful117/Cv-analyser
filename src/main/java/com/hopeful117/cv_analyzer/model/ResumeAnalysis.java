package com.hopeful117.cv_analyzer.model;

import lombok.Data;

import java.util.List;

@Data
public class ResumeAnalysis {
    private int score;
    private List<String> atsRisks;
    private List<String> recommendations;

    public ResumeAnalysis(int score, List<String> atsRisks, List<String> recommendations) {
        this.score = score;
        this.atsRisks = atsRisks;
        this.recommendations = recommendations;
    }
}
