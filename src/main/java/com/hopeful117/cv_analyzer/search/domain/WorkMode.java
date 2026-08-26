package com.hopeful117.cv_analyzer.search.domain;

/**
 * Modes de travail que l'utilisateur accepte. Un ensemble vide signifie « ouvert à tous les modes ».
 * Vocabulaire aligné avec RemoteMode côté offre, mais forme multi-valuée côté préférences.
 */
public enum WorkMode {
    ONSITE("Sur site"),
    HYBRID("Hybride"),
    REMOTE("Télétravail");

    private final String frenchLabel;

    WorkMode(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
