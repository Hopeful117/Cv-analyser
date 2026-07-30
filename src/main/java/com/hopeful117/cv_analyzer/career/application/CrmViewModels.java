package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class CrmViewModels {
    private CrmViewModels() {
    }

    public record ApplicationListItem(
            Long id, String company, String jobTitle, ContractType contractType,
            String contractRaw, LocalDate appliedAt, ApplicationStatus status,
            ApplicationPriority priority, LocalDate followUpPlannedAt,
            boolean hasResume, boolean hasCoverLetter, ProjectionStatus projectionStatus) {
    }

    public record StatusHistoryItem(
            ApplicationStatus previousStatus, ApplicationStatus newStatus,
            Instant changedAt, ChangeSource source, String comment) {
    }

    public record ProjectionView(
            ProjectionStatus status, Instant lastAttemptAt, Instant lastSuccessfulSyncAt,
            String lastErrorCode, String lastErrorMessage, int retryCount) {
    }

    public record ApplicationDetails(
            Long id, Long opportunityId, Long companyId, String companyName, String city,
            String address, String phone, String email, String website, String jobTitle,
            String offerUrl, ContractType contractType, String contractRaw,
            WorkSchedule workSchedule, String workScheduleRaw, RemoteMode remoteMode,
            String source, String salary, String distance, String location, String description,
            ApplicationStatus status, ApplicationPriority priority, LocalDate appliedAt,
            LocalDate followUpPlannedAt, LocalDate lastFollowUpAt,
            InterviewStatus interviewStatus, ApplicationDecision decision,
            boolean portfolioSent, String notes, String privateNotes,
            Long resumeVersionId, Integer resumeVersionNumber, Long coverLetterId,
            Long analysisId, Integer aiScore, Instant createdAt, Instant updatedAt,
            List<StatusHistoryItem> history, ProjectionView projection) {
    }

    public record DocumentOption(Long id, String label) {
    }

    public record FormOptions(List<DocumentOption> resumeVersions,
                              List<DocumentOption> coverLetters,
                              List<DocumentOption> analyses) {
    }

    public record CrmDashboard(
            long opportunities, long applicationsSent, long waiting, long interviews,
            long followUpsDue, long rejected, long success, long synchronizationErrors,
            List<ApplicationListItem> recent, List<ApplicationListItem> followUps,
            List<ApplicationListItem> priorities, List<StatusHistoryItem> transitions) {
    }
}
