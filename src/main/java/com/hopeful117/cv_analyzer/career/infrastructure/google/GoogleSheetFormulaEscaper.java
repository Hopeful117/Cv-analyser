package com.hopeful117.cv_analyzer.career.infrastructure.google;

public final class GoogleSheetFormulaEscaper {
    private GoogleSheetFormulaEscaper() {}

    public static String escape(String value) {
        if (value == null || value.isEmpty()) return value;
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' ? "'" + value : value;
    }
}
