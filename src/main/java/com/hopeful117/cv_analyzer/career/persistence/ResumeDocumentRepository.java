package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeDocumentRepository extends JpaRepository<ResumeDocumentEntity, Long> {
    @EntityGraph(attributePaths = {"analysis", "analysis.opportunity", "versions"})
    Optional<ResumeDocumentEntity> findOneById(Long id);

    @EntityGraph(attributePaths = {"analysis", "analysis.opportunity", "versions"})
    Optional<ResumeDocumentEntity> findByAnalysisId(Long analysisId);

    @EntityGraph(attributePaths = {"analysis", "analysis.opportunity", "versions"})
    List<ResumeDocumentEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
