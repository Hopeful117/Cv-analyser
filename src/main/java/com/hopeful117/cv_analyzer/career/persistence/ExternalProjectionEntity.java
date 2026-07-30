package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "career_external_projection", uniqueConstraints =
        @UniqueConstraint(name = "uk_external_projection_resource",
                columnNames = {"resource_type", "resource_id", "spreadsheet_id", "sheet_name"}))
@Getter
@Setter
@NoArgsConstructor
public class ExternalProjectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    @Column(name = "spreadsheet_id", nullable = false, length = 200)
    private String spreadsheetId;
    @Column(name = "sheet_name", nullable = false, length = 200)
    private String sheetName;
    @Column(name = "external_id", nullable = false, length = 120)
    private String externalId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectionStatus status;
    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;
    @Column(name = "last_successful_sync_at")
    private Instant lastSuccessfulSyncAt;
    @Column(name = "last_error_code", length = 80)
    private String lastErrorCode;
    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
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
