package com.hopeful117.cv_analyzer.career.application.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GoogleSheetsProjectionPort {
    ConnectionReport validateConnection();
    List<String> readHeaders();
    Optional<RemoteProjection> findByExternalId(String externalId);
    UpsertResult upsert(ApplicationSheetProjection projection);
    UpsertResult updateLegacyRow(int rowNumber, ApplicationSheetProjection projection);
    RebuildReport rebuild(List<ApplicationSheetProjection> projections);

    record ConnectionReport(boolean success, String sheetName, int headerCount,
                            List<String> missingColumns, String errorCode, String message) {}
    record RemoteProjection(int rowNumber, Map<String, String> values) {}
    record UpsertResult(String externalId, int rowNumber, boolean created) {}
    record RebuildReport(int requested, int updated, int appended, int failed,
                         List<String> errors) {}
}
