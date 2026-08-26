package com.hopeful117.cv_analyzer.career.domain;

public enum ContractType {
    CDI, CDD, ALTERNANCE, STAGE, FREELANCE, INTERIM, OTHER;

    public static ContractType fromCode(String code) {
        if (code == null) return null;
        return switch (code.trim().toUpperCase()) {
            case "CDI" -> CDI;
            case "CDD" -> CDD;
            case "ALTERNANCE" -> ALTERNANCE;
            case "STAGE" -> STAGE;
            case "FREELANCE" -> FREELANCE;
            case "INTERIM" -> INTERIM;
            default -> null;
        };
    }
}
