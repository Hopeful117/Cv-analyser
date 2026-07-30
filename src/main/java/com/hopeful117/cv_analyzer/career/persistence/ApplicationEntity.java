package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "career_application")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ApplicationStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApplicationPriority priority;
    @Column(name = "applied_at")
    private LocalDate appliedAt;
    @Column(name = "follow_up_planned_at")
    private LocalDate followUpPlannedAt;
    @Column(name = "last_follow_up_at")
    private LocalDate lastFollowUpAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_status", nullable = false, length = 20)
    private InterviewStatus interviewStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationDecision decision;
    @Column(name = "portfolio_sent", nullable = false)
    private boolean portfolioSent;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String notes;
    @Lob
    @Column(name = "private_notes", columnDefinition = "LONGTEXT")
    private String privateNotes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_version_id")
    private ResumeVersionEntity resumeVersion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_id")
    private CoverLetterEntity coverLetter;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private ResumeAnalysisRecordEntity analysis;
    @Column(name = "legacy_external_id", length = 120)
    private String legacyExternalId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void createTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
