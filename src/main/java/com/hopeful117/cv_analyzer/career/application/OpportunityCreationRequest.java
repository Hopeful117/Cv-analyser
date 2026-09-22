package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.career.domain.OpportunitySourceType;
import com.hopeful117.cv_analyzer.career.domain.OpportunityStatus;
import com.hopeful117.cv_analyzer.career.domain.RemoteMode;
import com.hopeful117.cv_analyzer.career.domain.WorkSchedule;
import com.hopeful117.cv_analyzer.career.persistence.CompanyEntity;

public record OpportunityCreationRequest(
        CompanyEntity company,
        String title,
        String companyName,
        ContractType contractType,
        String contractTypeRaw,
        WorkSchedule workSchedule,
        String workScheduleRaw,
        RemoteMode remoteMode,
        String source,
        String salaryText,
        String distanceText,
        String location,
        OpportunitySourceType sourceType,
        String sourceUrl,
        String rawDescription,
        String normalizedDescription,
        String detectedLanguage,
        OpportunityStatus status
) {
}
