package com.hopeful117.cv_analyzer.career.infrastructure.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "career.google-sheets")
public record CareerGoogleSheetsProperties(
        boolean enabled, String spreadsheetId, String applicationsSheet,
        String dashboardSheet, int headerRow) {
    public CareerGoogleSheetsProperties {
        applicationsSheet = applicationsSheet == null || applicationsSheet.isBlank() ? "Candidatures" : applicationsSheet;
        dashboardSheet = dashboardSheet == null || dashboardSheet.isBlank() ? "Tableau de bord" : dashboardSheet;
        headerRow = headerRow < 1 ? 1 : headerRow;
    }

    public boolean configured() {
        return enabled && spreadsheetId != null && !spreadsheetId.isBlank();
    }

    public String maskedSpreadsheetId() {
        if (spreadsheetId == null || spreadsheetId.isBlank()) return "Non configuré";
        return spreadsheetId.length() < 10 ? "••••" : spreadsheetId.substring(0, 4) + "••••" +
                spreadsheetId.substring(spreadsheetId.length() - 4);
    }
}
