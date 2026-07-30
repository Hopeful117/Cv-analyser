package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsProjectionPort;
import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import com.hopeful117.cv_analyzer.career.infrastructure.google.CareerGoogleSheetsProperties;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetsFunctionalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationProjectionService {
    private final GoogleSheetsProjectionPort port;
    private final CareerGoogleSheetsProperties properties;
    private final ProjectionStateService stateService;
    private final ApplicationProjectionQueryService queryService;

    public void synchronize(long applicationId) {
        var projection = queryService.get(applicationId, Instant.now(), ProjectionStatus.SYNCHRONIZED);
        stateService.markPending(applicationId);
        if (!properties.configured()) return;
        try {
            port.upsert(projection);
            stateService.markSuccess(applicationId);
        } catch (GoogleSheetsFunctionalException exception) {
            fail(applicationId, exception.getCode(), exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            fail(applicationId, "UNEXPECTED_GOOGLE_ERROR",
                    "La synchronisation Google Sheets a échoué.", exception);
        }
    }

    private void fail(long id, String code, String message, RuntimeException exception) {
        stateService.markFailure(id, code, message);
        log.warn("Google Sheets projection failed for application {} with code {}: {}",
                id, code, exception.getMessage());
    }
}
