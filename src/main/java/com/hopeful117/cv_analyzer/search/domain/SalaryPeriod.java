package com.hopeful117.cv_analyzer.search.domain;

/**
 * Périodicité du salaire minimum exprimé par l'utilisateur. Hypothèse documentée : montants bruts.
 */
public enum SalaryPeriod {
    ANNUAL("Annuel"),
    MONTHLY("Mensuel");

    private final String frenchLabel;

    SalaryPeriod(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }

    public String getFrenchLabel() {
        return frenchLabel;
    }
}
