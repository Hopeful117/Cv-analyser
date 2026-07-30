package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsProjectionPort;
import com.hopeful117.cv_analyzer.career.domain.ProjectionStatus;
import com.hopeful117.cv_analyzer.career.infrastructure.google.CareerGoogleSheetsProperties;
import com.hopeful117.cv_analyzer.career.persistence.ExternalProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectionAdministrationService {
    private final GoogleSheetsProjectionPort port;
    private final CareerGoogleSheetsProperties properties;
    private final ExternalProjectionRepository projectionRepository;
    private final ApplicationProjectionQueryService queryService;
    private final ApplicationProjectionService projectionService;
    private final ProjectionStateService stateService;

    public SettingsView settings() {
        return new SettingsView(properties.enabled(), properties.configured(),
                properties.maskedSpreadsheetId(), properties.applicationsSheet(),
                projectionRepository.countByStatus(ProjectionStatus.FAILED));
    }

    public GoogleSheetsProjectionPort.ConnectionReport testConnection() {
        return port.validateConnection();
    }

    public GoogleSheetsProjectionPort.RebuildReport rebuild() {
        if (!properties.configured()) {
            return new GoogleSheetsProjectionPort.RebuildReport(0, 0, 0, 0,
                    List.of("L’intégration Google Sheets est désactivée ou incomplète."));
        }
        var projections = queryService.getAll();
        projections.forEach(p -> stateService.markPending(Long.parseLong(p.displayId())));
        var report = port.rebuild(projections);
        for (var projection : projections) {
            String prefix = projection.careerIntelligenceId() + " :";
            var failure = report.errors().stream().filter(error -> error.startsWith(prefix)).findFirst();
            if (failure.isPresent()) {
                stateService.markFailure(Long.parseLong(projection.displayId()), "REBUILD_ITEM_FAILED",
                        failure.get().substring(prefix.length()).trim());
            } else {
                stateService.markSuccess(Long.parseLong(projection.displayId()));
            }
        }
        return report;
    }

    public int retryFailures() {
        var failures = projectionRepository.findByStatusOrderByUpdatedAtAsc(ProjectionStatus.FAILED);
        failures.forEach(state -> projectionService.synchronize(state.getResourceId()));
        return failures.size();
    }

    public record SettingsView(boolean enabled, boolean configured, String maskedSpreadsheetId,
                               String sheetName, long failureCount) {}
}
