package com.hopeful117.cv_analyzer.career.application.port;

import com.hopeful117.cv_analyzer.career.application.consultation.GoogleSheetApplicationRow;
import com.hopeful117.cv_analyzer.career.application.consultation.GoogleSheetConsultationReport;

import java.util.Optional;

public interface GoogleSheetsConsultationPort {
    GoogleSheetConsultationReport readApplications();

    Optional<GoogleSheetApplicationRow> findByCareerIntelligenceId(String careerIntelligenceId);
}
