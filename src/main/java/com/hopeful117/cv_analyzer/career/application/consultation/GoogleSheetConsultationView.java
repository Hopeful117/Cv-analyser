package com.hopeful117.cv_analyzer.career.application.consultation;

import org.springframework.data.domain.Page;

import java.time.Instant;

public record GoogleSheetConsultationView(
        Page<GoogleSheetComparisonResult> results,
        long totalSheetRows,
        long synchronizedCount,
        long differentCount,
        long missingInCrmCount,
        long missingInSheetCount,
        long duplicateCount,
        long invalidCount,
        Instant readAt) {
}
