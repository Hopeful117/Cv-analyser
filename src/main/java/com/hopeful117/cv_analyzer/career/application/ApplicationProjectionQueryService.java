package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import com.hopeful117.cv_analyzer.career.persistence.*;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationProjectionQueryService {
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public ApplicationSheetProjection get(long id, Instant synchronizedAt, ProjectionStatus status) {
        return map(applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidature introuvable.")),
                synchronizedAt, status);
    }

    @Transactional(readOnly = true)
    public List<ApplicationSheetProjection> getAll() {
        Instant now = Instant.now();
        return applicationRepository.findAll(Sort.by("id")).stream()
                .map(entity -> map(entity, now, ProjectionStatus.PENDING)).toList();
    }

    @Transactional(readOnly = true)
    public Integer getLegacySheetRow(long id) {
        String legacyId = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidature introuvable."))
                .getLegacyExternalId();
        String prefix = "GOOGLE-SHEET-ROW:";
        if (legacyId == null || !legacyId.startsWith(prefix)) return null;
        try {
            return Integer.valueOf(legacyId.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private ApplicationSheetProjection map(ApplicationEntity application, Instant synchronizedAt,
                                           ProjectionStatus projectionStatus) {
        OpportunityEntity opportunity = application.getOpportunity();
        CompanyEntity company = opportunity.getCompany();
        String companyName = company == null ? opportunity.getCompanyName() : company.getName();
        return new ApplicationSheetProjection(
                String.valueOf(application.getId()), "APPLICATION-" + application.getId(), companyName,
                company == null ? null : company.getCity(), company == null ? null : company.getAddress(),
                company == null ? null : company.getPhone(), company == null ? null : company.getEmail(),
                company == null ? null : company.getWebsite(), opportunity.getTitle(),
                opportunity.getSourceUrl(), label(opportunity.getContractType(), opportunity.getContractTypeRaw()),
                label(opportunity.getWorkSchedule(), opportunity.getWorkScheduleRaw()),
                opportunity.getRemoteMode() == null ? null : opportunity.getRemoteMode().name(),
                opportunity.getSource(), application.getResumeVersion() != null,
                application.getCoverLetter() != null, application.isPortfolioSent(), application.getAppliedAt(),
                application.getFollowUpPlannedAt(), application.getLastFollowUpAt(),
                application.getStatus().getFrenchLabel(), application.getInterviewStatus().name(),
                application.getDecision().name(), opportunity.getSalaryText(), opportunity.getDistanceText(),
                application.getPriority().getFrenchLabel(), application.getNotes(),
                application.getAnalysis() == null ? null : application.getAnalysis().getOverallScore(),
                application.getResumeVersion() == null ? null : application.getResumeVersion().getVersionNumber(),
                synchronizedAt, projectionStatus.name());
    }

    private String label(Enum<?> value, String raw) {
        return value == null ? raw : value.name();
    }
}
