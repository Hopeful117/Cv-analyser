package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeAnalysisRecordRepository extends JpaRepository<ResumeAnalysisRecordEntity, Long> {
    @EntityGraph(attributePaths = "opportunity")
    Page<ResumeAnalysisRecordEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "opportunity")
    Optional<ResumeAnalysisRecordEntity> findOneById(Long id);

    long countByOpportunityId(Long opportunityId);
}
