package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsProjectionPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "career.google-sheets", name = "enabled",
        havingValue = "false", matchIfMissing = true)
public class DisabledGoogleSheetsProjectionAdapter implements GoogleSheetsProjectionPort {
    private GoogleSheetsFunctionalException disabled() {
        return new GoogleSheetsFunctionalException("INTEGRATION_DISABLED",
                "La projection Google Sheets est désactivée ou incomplètement configurée.");
    }
    @Override public ConnectionReport validateConnection() {
        return new ConnectionReport(false, "", 0, List.of(), "INTEGRATION_DISABLED", disabled().getMessage());
    }
    @Override public List<String> readHeaders() { throw disabled(); }
    @Override public Optional<RemoteProjection> findByExternalId(String externalId) { throw disabled(); }
    @Override public UpsertResult upsert(ApplicationSheetProjection projection) { throw disabled(); }
    @Override public RebuildReport rebuild(List<ApplicationSheetProjection> projections) { throw disabled(); }
}
