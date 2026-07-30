package com.hopeful117.cv_analyzer.career.application.consultation;

import java.util.List;
import java.util.Map;

public record GoogleSheetApplicationDetailView(
        String careerIntelligenceId,
        GoogleSheetComparisonResult comparison,
        List<GoogleSheetApplicationRow> matchingSheetRows,
        Map<String, String> crmColumns) {
}
