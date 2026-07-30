package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository
        extends JpaRepository<ApplicationStatusHistoryEntity, Long> {
    List<ApplicationStatusHistoryEntity> findByApplicationIdOrderByChangedAtDesc(Long applicationId);
    List<ApplicationStatusHistoryEntity> findAllByOrderByChangedAtDesc(Pageable pageable);
}
