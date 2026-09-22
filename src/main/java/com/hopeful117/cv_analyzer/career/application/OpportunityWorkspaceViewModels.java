package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.*;

import java.time.Instant;
import java.util.List;

public final class OpportunityWorkspaceViewModels {
    private OpportunityWorkspaceViewModels() {
    }

    public record OpportunityListItem(
            Long id, String title, String companyName, OpportunityStatus status,
            Instant createdAt, int analysisCount, int resumeCount, int letterCount,
            int applicationCount) {
        public String displayTitle() {
            return title == null || title.isBlank() ? "Opportunité sans titre" : title;
        }

        public String displayCompany() {
            return companyName == null || companyName.isBlank()
                    ? "Entreprise non renseignée" : companyName;
        }
    }

    public record OpportunityDetails(
            Long id, String title, String companyName, String location,
            ContractType contractType, String contractTypeRaw, RemoteMode remoteMode,
            String source, String sourceUrl, String description, OpportunityStatus status,
            Instant createdAt) {
        public String displayTitle() {
            return title == null || title.isBlank() ? "Opportunité sans titre" : title;
        }

        public String displayCompany() {
            return companyName == null || companyName.isBlank()
                    ? "Entreprise non renseignée" : companyName;
        }
    }

    public record AnalysisItem(Long id, Instant createdAt, int overallScore, int matchScore) {
    }

    public record ResumeItem(Long id, Long analysisId, String professionalTitle,
                             int latestVersion, Instant updatedAt) {
    }

    public record LetterItem(Long id, CoverLetterOrigin origin, Instant updatedAt) {
    }

    public record Workspace(
            OpportunityDetails opportunity,
            List<AnalysisItem> analyses,
            List<ResumeItem> resumes,
            List<LetterItem> letters,
            List<CrmViewModels.ApplicationListItem> applications) {
    }
}
