package com.hopeful117.cv_analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@Table(name = "interview_session")
@Entity
@Getter
@Setter
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sessionId;

    @Lob
    private String cv;
    @Lob
    private String jobDescription;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<InterviewQuestion> questions;

    private int currentIndex;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL,
            orphanRemoval = true)

    private List<InterviewQuestionResult> results;
}
