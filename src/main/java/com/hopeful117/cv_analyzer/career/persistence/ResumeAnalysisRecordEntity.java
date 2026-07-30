package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.AnalysisNature;
import com.hopeful117.cv_analyzer.career.domain.GenerationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_resume_analysis")
@Getter
@Setter
@NoArgsConstructor
public class ResumeAnalysisRecordEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "overall_score", nullable = false)
    private int overallScore;
    @Column(name = "quality_score", nullable = false)
    private int qualityScore;
    @Column(name = "ats_score", nullable = false)
    private int atsScore;
    @Column(name = "match_score", nullable = false)
    private int matchScore;
    @Column(name = "job_offer_language", length = 16)
    private String jobOfferLanguage;
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_nature", nullable = false, length = 24)
    private AnalysisNature analysisNature;
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

    @ElementCollection
    @CollectionTable(name = "career_analysis_risk", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "item_value", nullable = false, length = 1000)
    @OrderColumn(name = "item_order")
    private List<String> risks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "career_analysis_recommendation", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "item_value", nullable = false, length = 1000)
    @OrderColumn(name = "item_order")
    private List<String> recommendations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "career_analysis_missing_keyword", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "item_value", nullable = false, length = 255)
    @OrderColumn(name = "item_order")
    private List<String> missingKeywords = new ArrayList<>();

    @PrePersist
    void createTimestamp() {
        createdAt = Instant.now();
    }
}
