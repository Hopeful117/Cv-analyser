package com.hopeful117.cv_analyzer.career.application.port;

import java.time.Instant;
import java.time.LocalDate;

public record ApplicationSheetProjection(
        String displayId, String careerIntelligenceId, String companyName, String city,
        String address, String phone, String email, String website, String jobTitle,
        String offerUrl, String contractType, String workSchedule, String remoteMode,
        String source, boolean resumeSent, boolean coverLetterSent, boolean portfolioSent,
        LocalDate appliedAt, LocalDate followUpPlannedAt, LocalDate lastFollowUpAt,
        String status, String interview, String decision, String salary, String distance,
        String priority, String notes, Integer aiScore, Integer resumeVersion,
        Instant lastSynchronizedAt, String synchronizationStatus) {
}
