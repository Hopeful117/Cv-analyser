package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalProjectionRepository extends JpaRepository<ExternalProjectionEntity, Long> {
    Optional<ExternalProjectionEntity> findByResourceTypeAndResourceIdAndSpreadsheetIdAndSheetName(
            String resourceType, Long resourceId, String spreadsheetId, String sheetName);
    Optional<ExternalProjectionEntity> findFirstByResourceTypeAndResourceIdOrderByUpdatedAtDesc(
            String resourceType, Long resourceId);
    long countByStatus(ProjectionStatus status);
    List<ExternalProjectionEntity> findByStatusOrderByUpdatedAtAsc(ProjectionStatus status);
    List<ExternalProjectionEntity> findByStatusOrderByUpdatedAtDesc(
            ProjectionStatus status, Pageable pageable);
}
