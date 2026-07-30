package com.hopeful117.cv_analyzer.career.application.consultation;

import java.time.Instant;
import java.util.List;

public record GoogleSheetApplicationSnapshot(
        List<String> headers,
        List<GoogleSheetApplicationRow> rows,
        Instant readAt) {
}
