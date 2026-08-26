package com.hopeful117.cv_analyzer.profile.domain;

public enum EducationKind {
    EDUCATION("Formation"),
    CERTIFICATION("Certification");

    private final String frenchLabel;

    EducationKind(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
