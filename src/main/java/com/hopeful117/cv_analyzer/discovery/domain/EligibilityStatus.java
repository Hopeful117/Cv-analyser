package com.hopeful117.cv_analyzer.discovery.domain;

public enum EligibilityStatus {
    ELIGIBLE("Éligible"),
    INELIGIBLE("Non éligible"),
    REVIEW_REQUIRED("À vérifier");

    private final String label;

    EligibilityStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
