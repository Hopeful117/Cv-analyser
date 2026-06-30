package com.hopeful117.cv_analyzer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "interview_report")
@Getter
@Setter
public class InterviewReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    private int overallScore;

    private List<String> globalStrengths;
    private List<String> globalImprovements;

    @OneToMany(mappedBy = "resultId")
    private List<InterviewQuestionResult> results;
}
