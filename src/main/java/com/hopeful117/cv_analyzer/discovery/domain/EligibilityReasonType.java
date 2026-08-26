package com.hopeful117.cv_analyzer.discovery.domain;

public enum EligibilityReasonType {
    CONTRACT_NOT_ACCEPTED("Contrat non accepté"),
    CONTRACT_UNKNOWN("Contrat inconnu"),
    LOCATION_NOT_ACCEPTED("Localisation non acceptée"),
    LOCATION_UNKNOWN("Localisation inconnue"),
    WORK_MODE_NOT_ACCEPTED("Mode de travail non accepté"),
    WORK_MODE_UNKNOWN("Mode de travail inconnu"),
    EXCLUDED_TECHNOLOGY_FOUND("Technologie exclue détectée"),
    SALARY_BELOW_MINIMUM("Salaire inférieur au minimum"),
    SALARY_UNKNOWN("Salaire inconnu"),
    PREFERRED_TECHNOLOGY_FOUND("Technologie recherchée détectée");

    private final String label;

    EligibilityReasonType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
