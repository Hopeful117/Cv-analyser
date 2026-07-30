package com.hopeful117.cv_analyzer.career.persistence;

import com.hopeful117.cv_analyzer.career.domain.ApplicationPriority;
import com.hopeful117.cv_analyzer.career.domain.ApplicationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long>,
        JpaSpecificationExecutor<ApplicationEntity> {
    long countByStatus(ApplicationStatus status);
    long countByStatusIn(Collection<ApplicationStatus> statuses);
    long countByFollowUpPlannedAtLessThanEqualAndStatusNotIn(
            LocalDate date, Collection<ApplicationStatus> statuses);
    List<ApplicationEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    List<ApplicationEntity> findByFollowUpPlannedAtLessThanEqualAndStatusNotInOrderByFollowUpPlannedAtAsc(
            LocalDate date, Collection<ApplicationStatus> statuses, Pageable pageable);
    List<ApplicationEntity> findByPriorityAndStatusNotInOrderByUpdatedAtDesc(
            ApplicationPriority priority, Collection<ApplicationStatus> statuses, Pageable pageable);
    boolean existsByOpportunityIdAndAppliedAt(Long opportunityId, LocalDate appliedAt);
    boolean existsByLegacyExternalId(String legacyExternalId);
    boolean existsByOpportunityCompanyNameIgnoreCaseAndOpportunityTitleIgnoreCaseAndAppliedAt(
            String companyName, String title, LocalDate appliedAt);
}
