package com.hopeful117.cv_analyzer.profile.domain;

public enum SkillOrigin {
    MANUAL("Saisie manuelle"),
    FROM_CV("Issue du CV");

    private final String frenchLabel;

    SkillOrigin(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
