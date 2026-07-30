package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.OpportunitySourceType;
import com.hopeful117.cv_analyzer.career.domain.OpportunityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "career_opportunity")
@Getter
@Setter
@NoArgsConstructor
public class OpportunityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyEntity company;
    @Column(length = 200)
    private String title;
    @Column(name = "company_name", length = 200)
    private String companyName;
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 24)
    private com.hopeful117.cv_analyzer.career.domain.ContractType contractType;
    @Column(name = "contract_type_raw", length = 120)
    private String contractTypeRaw;
    @Enumerated(EnumType.STRING)
    @Column(name = "work_schedule", length = 24)
    private com.hopeful117.cv_analyzer.career.domain.WorkSchedule workSchedule;
    @Column(name = "work_schedule_raw", length = 120)
    private String workScheduleRaw;
    @Enumerated(EnumType.STRING)
    @Column(name = "remote_mode", length = 24)
    private com.hopeful117.cv_analyzer.career.domain.RemoteMode remoteMode;
    @Column(length = 200)
    private String source;
    @Column(name = "salary_text", length = 200)
    private String salaryText;
    @Column(name = "distance_text", length = 120)
    private String distanceText;
    @Column(length = 300)
    private String location;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private OpportunitySourceType sourceType;
    @Column(name = "source_url", length = 2048)
    private String sourceUrl;
    @Lob
    @Column(name = "raw_description", nullable = false, columnDefinition = "LONGTEXT")
    private String rawDescription;
    @Lob
    @Column(name = "normalized_description", nullable = false, columnDefinition = "LONGTEXT")
    private String normalizedDescription;
    @Column(name = "detected_language", length = 16)
    private String detectedLanguage;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStatus status;
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
