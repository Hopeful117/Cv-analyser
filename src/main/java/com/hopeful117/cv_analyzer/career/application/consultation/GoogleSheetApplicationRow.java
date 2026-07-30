package com.hopeful117.cv_analyzer.career.application.consultation;

import java.util.Map;

public record GoogleSheetApplicationRow(
        int rowNumber,
        Map<String, String> columns,
        String companyName,
        String jobTitle,
        String status,
        String priority,
        String appliedAt,
        String followUpPlannedAt,
        String careerIntelligenceId,
        String synchronizationStatus,
        boolean valid) {
}
