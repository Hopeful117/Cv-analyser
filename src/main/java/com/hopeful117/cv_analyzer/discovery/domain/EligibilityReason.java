package com.hopeful117.cv_analyzer.discovery.domain;

public record EligibilityReason(
        EligibilityReasonType type,
        String message
) {
    public static EligibilityReason of(EligibilityReasonType type, String message) {
        return new EligibilityReason(type, message);
    }
}
