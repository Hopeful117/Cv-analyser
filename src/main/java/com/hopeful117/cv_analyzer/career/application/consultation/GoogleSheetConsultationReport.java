package com.hopeful117.cv_analyzer.career.application.consultation;

import java.time.Instant;
import java.util.List;

public record GoogleSheetConsultationReport(
        GoogleSheetApplicationSnapshot snapshot,
        List<String> warnings,
        List<String> missingColumns,
        Instant generatedAt) {
}
