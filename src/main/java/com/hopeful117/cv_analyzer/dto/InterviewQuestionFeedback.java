package com.hopeful117.cv_analyzer.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionFeedback {

    private Long feedbackId;

    private int technicalAccuracy;
    private int clarity;
    private int confidenceLevel;
    private int relevance;

    private List<String> strengths;
    private List<String> improvements;

    private String suggestedAnswer;
}
