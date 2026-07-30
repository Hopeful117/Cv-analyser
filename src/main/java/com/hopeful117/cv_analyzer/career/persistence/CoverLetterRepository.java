package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetterEntity, Long> {
    @EntityGraph(attributePaths = "opportunity")
    Optional<CoverLetterEntity> findOneById(Long id);

    @EntityGraph(attributePaths = "opportunity")
    List<CoverLetterEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    long countByOpportunityId(Long opportunityId);
}
