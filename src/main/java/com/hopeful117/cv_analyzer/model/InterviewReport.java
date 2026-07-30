package com.hopeful117.cv_analyzer.model;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InterviewReport {
    private Long reportId;
    private InterviewSession session;
    private int overallScore;
    private List<String> globalStrengths;
    private List<String> globalImprovements;
    private List<InterviewQuestionResult> results;
}
