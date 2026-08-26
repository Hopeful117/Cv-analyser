package com.hopeful117.cv_analyzer.discovery.domain;

import java.util.Collections;
import java.util.List;

public record EligibilityResult(
        EligibilityStatus status,
        List<EligibilityReason> reasons
) {
    public static EligibilityResult eligible() {
        return new EligibilityResult(EligibilityStatus.ELIGIBLE, Collections.emptyList());
    }

    public static EligibilityResult ineligible(EligibilityReason reason) {
        return new EligibilityResult(EligibilityStatus.INELIGIBLE, List.of(reason));
    }

    public static EligibilityResult reviewRequired(EligibilityReason reason) {
        return new EligibilityResult(EligibilityStatus.REVIEW_REQUIRED, List.of(reason));
    }

    public static EligibilityResult of(EligibilityStatus status, List<EligibilityReason> reasons) {
        return new EligibilityResult(status, reasons);
    }
}
