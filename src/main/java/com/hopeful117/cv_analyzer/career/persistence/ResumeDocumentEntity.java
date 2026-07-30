package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_resume_document")
@Getter
@Setter
@NoArgsConstructor
public class ResumeDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, unique = true)
    private ResumeAnalysisRecordEntity analysis;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("versionNumber DESC")
    private List<ResumeVersionEntity> versions = new ArrayList<>();

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

    public void addVersion(ResumeVersionEntity version) {
        version.setDocument(this);
        versions.add(version);
        updatedAt = Instant.now();
    }
}
