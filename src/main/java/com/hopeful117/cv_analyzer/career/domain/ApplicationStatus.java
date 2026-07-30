package com.hopeful117.cv_analyzer.career.domain;

public enum ApplicationStatus {
    NOT_CONTACTED("Non démarché"),
    APPLIED("Candidature envoyée"),
    WAITING("En attente"),
    INTERVIEW("Entretien"),
    FOLLOWED_UP("Relance effectuée"),
    REJECTED("Refus"),
    SUCCESS("Succès"),
    ARCHIVED("Archivé");

    private final String frenchLabel;

    ApplicationStatus(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
