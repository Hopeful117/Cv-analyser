package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.CoverLetterOrigin;
import com.hopeful117.cv_analyzer.career.domain.DocumentStatus;
import com.hopeful117.cv_analyzer.career.domain.GenerationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "career_cover_letter")
@Getter
@Setter
@NoArgsConstructor
public class CoverLetterEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private ResumeAnalysisRecordEntity analysis;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_version_id")
    private ResumeVersionEntity resumeVersion;
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    @Column(length = 16)
    private String language;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CoverLetterOrigin origin;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "ai_provider", length = 80)
    private String aiProvider;
    @Column(name = "ai_model", length = 120)
    private String aiModel;
    @Column(name = "prompt_version", length = 80)
    private String promptVersion;
    @Enumerated(EnumType.STRING)
    @Column(name = "generation_type", length = 40)
    private GenerationType generationType;
    @Column(name = "generated_at")
    private Instant generatedAt;

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
