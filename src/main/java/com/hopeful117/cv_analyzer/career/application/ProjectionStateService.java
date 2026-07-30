package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import com.hopeful117.cv_analyzer.career.infrastructure.google.CareerGoogleSheetsProperties;
import com.hopeful117.cv_analyzer.career.persistence.ExternalProjectionEntity;
import com.hopeful117.cv_analyzer.career.persistence.ExternalProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProjectionStateService {
    private static final String RESOURCE = "APPLICATION";
    private final ExternalProjectionRepository repository;
    private final CareerGoogleSheetsProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalProjectionEntity markPending(long applicationId) {
        ExternalProjectionEntity state = getOrCreate(applicationId);
        state.setStatus(properties.configured() ? ProjectionStatus.PENDING : ProjectionStatus.DISABLED);
        state.setLastAttemptAt(Instant.now());
        state.setLastErrorCode(null);
        state.setLastErrorMessage(null);
        return repository.save(state);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(long applicationId) {
        ExternalProjectionEntity state = getOrCreate(applicationId);
        Instant now = Instant.now();
        state.setStatus(ProjectionStatus.SYNCHRONIZED);
        state.setLastAttemptAt(now);
        state.setLastSuccessfulSyncAt(now);
        state.setLastErrorCode(null);
        state.setLastErrorMessage(null);
        state.setRetryCount(0);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailure(long applicationId, String code, String message) {
        ExternalProjectionEntity state = getOrCreate(applicationId);
        state.setStatus(ProjectionStatus.FAILED);
        state.setLastAttemptAt(Instant.now());
        state.setLastErrorCode(truncate(code, 80));
        state.setLastErrorMessage(truncate(message, 1000));
        state.setRetryCount(state.getRetryCount() + 1);
    }

    private ExternalProjectionEntity getOrCreate(long applicationId) {
        String spreadsheet = properties.spreadsheetId() == null || properties.spreadsheetId().isBlank()
                ? "NOT_CONFIGURED" : properties.spreadsheetId();
        return repository.findByResourceTypeAndResourceIdAndSpreadsheetIdAndSheetName(
                RESOURCE, applicationId, spreadsheet, properties.applicationsSheet()).orElseGet(() -> {
                    ExternalProjectionEntity state = new ExternalProjectionEntity();
                    state.setResourceType(RESOURCE);
                    state.setResourceId(applicationId);
                    state.setSpreadsheetId(spreadsheet);
                    state.setSheetName(properties.applicationsSheet());
                    state.setExternalId("APPLICATION-" + applicationId);
                    state.setStatus(ProjectionStatus.PENDING);
                    return state;
                });
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
