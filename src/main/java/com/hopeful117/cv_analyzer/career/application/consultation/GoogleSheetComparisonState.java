package com.hopeful117.cv_analyzer.career.application.consultation;

public enum GoogleSheetComparisonState {
    SYNCHRONIZED("Synchronisée"),
    DIFFERENT("Différente"),
    MISSING_IN_CRM("Absente du CRM"),
    MISSING_IN_SHEET("Absente du Sheet"),
    DUPLICATE_EXTERNAL_ID("Identifiant dupliqué"),
    INVALID("Invalide");

    private final String frenchLabel;

    GoogleSheetComparisonState(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
