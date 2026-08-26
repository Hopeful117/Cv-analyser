package com.hopeful117.cv_analyzer.search.domain;

public enum TechnologyPreference {
    PREFERRED("Recherchée"),
    EXCLUDED("Exclue");

    private final String frenchLabel;

    TechnologyPreference(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
