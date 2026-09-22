package com.hopeful117.cv_analyzer.career.domain;

import java.util.Locale;

public enum ContractType {
    CDI, CDD, ALTERNANCE, STAGE, FREELANCE, INTERIM, OTHER;

    public static ContractType fromCode(String code) {
        if (code == null) return null;
        String normalized = code.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return switch (normalized) {
            case "CDI", "PERMANENT", "PERMANENT_EMPLOYMENT" -> CDI;
            case "CDD", "FIXED_TERM", "FIXED_TERM_EMPLOYMENT" -> CDD;
            case "ALTERNANCE", "APPRENTICESHIP" -> ALTERNANCE;
            case "STAGE", "INTERNSHIP" -> STAGE;
            case "FREELANCE" -> FREELANCE;
            case "INTERIM", "TEMPORARY" -> INTERIM;
            default -> null;
        };
    }
}
