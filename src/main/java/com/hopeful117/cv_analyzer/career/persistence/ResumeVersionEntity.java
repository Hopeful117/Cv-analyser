package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.GenerationType;
import com.hopeful117.cv_analyzer.career.domain.ResumeVersionOrigin;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_resume_version", uniqueConstraints =
        @UniqueConstraint(name = "uk_resume_version_number", columnNames = {"document_id", "version_number"}))
@Getter
@Setter
@NoArgsConstructor
public class ResumeVersionEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private ResumeDocumentEntity document;
    @Column(name = "version_number", nullable = false)
    private int versionNumber;
    @Column(name = "candidate_name", length = 200)
    private String candidateName;
    @Column(name = "professional_title", length = 200)
    private String professionalTitle;
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    @Column(length = 16)
    private String language;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ResumeVersionOrigin origin;
    @Enumerated(EnumType.STRING)
    @Column(name = "pdf_style", nullable = false, length = 30)
    private ResumePdfStyle pdfStyle = ResumePdfStyle.PROFESSIONAL;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
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
    @CollectionTable(name = "career_resume_placeholder", joinColumns = @JoinColumn(name = "version_id"))
    @Column(name = "item_value", nullable = false, length = 1000)
    @OrderColumn(name = "item_order")
    private List<String> placeholders = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "career_resume_correction", joinColumns = @JoinColumn(name = "version_id"))
    @Column(name = "item_value", nullable = false, length = 1000)
    @OrderColumn(name = "item_order")
    private List<String> corrections = new ArrayList<>();

    @PrePersist
    void createTimestamp() {
        createdAt = Instant.now();
    }
}
