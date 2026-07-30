package com.hopeful117.cv_analyzer.career.application.consultation;

import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;

import java.util.List;

public record GoogleSheetComparisonResult(
        GoogleSheetComparisonState state,
        GoogleSheetApplicationRow sheetRow,
        ApplicationSheetProjection crmApplication,
        Long crmApplicationId,
        List<GoogleSheetDifference> differences) {

    public String careerIntelligenceId() {
        return sheetRow != null ? sheetRow.careerIntelligenceId()
                : crmApplication == null ? null : crmApplication.careerIntelligenceId();
    }

    public String companyName() {
        return sheetRow != null ? sheetRow.companyName()
                : crmApplication == null ? null : crmApplication.companyName();
    }

    public String jobTitle() {
        return sheetRow != null ? sheetRow.jobTitle()
                : crmApplication == null ? null : crmApplication.jobTitle();
    }
}
