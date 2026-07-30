package com.hopeful117.cv_analyzer.career.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<OpportunityEntity, Long> {
    Page<OpportunityEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<OpportunityEntity> findAllByOrderByCreatedAtDesc();
    long countByCompanyId(Long companyId);
}
