package com.hopeful117.cv_analyzer.career.domain;

public enum ApplicationPriority {
    LOW("Faible"), MEDIUM("Moyenne"), HIGH("Haute");

    private final String frenchLabel;

    ApplicationPriority(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
