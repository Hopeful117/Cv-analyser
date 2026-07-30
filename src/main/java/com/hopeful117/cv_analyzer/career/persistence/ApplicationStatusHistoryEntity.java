package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.ApplicationStatus;
import com.hopeful117.cv_analyzer.career.domain.ChangeSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "career_application_status_history")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationEntity application;
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 24)
    private ApplicationStatus previousStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 24)
    private ApplicationStatus newStatus;
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "change_source", nullable = false, length = 16)
    private ChangeSource changeSource;
    @Column(length = 500)
    private String comment;

    @PrePersist
    void createTimestamp() {
        changedAt = Instant.now();
    }
}
