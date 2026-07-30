package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeVersionRepository extends JpaRepository<ResumeVersionEntity, Long> {
    @EntityGraph(attributePaths = {"document", "document.analysis"})
    Optional<ResumeVersionEntity> findOneById(Long id);
}
