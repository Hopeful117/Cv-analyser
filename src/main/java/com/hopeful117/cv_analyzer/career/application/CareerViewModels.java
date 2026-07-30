package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.model.GeneratedResume;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;

import java.time.Instant;
import java.util.List;

public final class CareerViewModels {
    private CareerViewModels() {
    }

    public record OpportunityView(Long id, String title, String companyName, String sourceUrl,
                                  String description, String language, OpportunityStatus status,
                                  Instant createdAt) {
        public String displayTitle() {
            return title == null || title.isBlank() ? "Opportunité sans titre" : title;
        }

        public String displayCompany() {
            return companyName == null || companyName.isBlank() ? "Entreprise non renseignée" : companyName;
        }
    }

    public record AnalysisListItem(Long id, OpportunityView opportunity, Instant createdAt,
                                   int overallScore, int atsScore, int matchScore,
                                   AnalysisNature nature) {
    }

    public record ResumeVersionView(Long id, int number, String candidateName,
                                    String professionalTitle, String content, String language,
                                    ResumeVersionOrigin origin, ResumePdfStyle style,
                                    Instant createdAt, List<String> placeholders,
                                    List<String> corrections) {
    }

    public record ResumeSummary(Long id, Long analysisId, String professionalTitle,
                                String opportunityTitle, int latestVersion, Instant updatedAt) {
    }

    public record CoverLetterView(Long id, OpportunityView opportunity, String content,
                                  String language, DocumentStatus status, CoverLetterOrigin origin,
                                  Instant createdAt, Instant updatedAt) {
    }

    public record AnalysisDetails(Long id, OpportunityView opportunity, ResumeAnalysis analysis,
                                  Long resumeDocumentId, GeneratedResume generatedResume,
                                  ResumeVersionView activeVersion, List<ResumeVersionView> versions,
                                  Instant createdAt, String provider, String model,
                                  String promptVersion) {
    }

    public record Dashboard(long opportunityCount, long analysisCount, long resumeCount,
                            long coverLetterCount, List<AnalysisListItem> analyses,
                            List<ResumeSummary> resumes, List<CoverLetterView> coverLetters) {
    }
}
