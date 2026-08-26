package com.hopeful117.cv_analyzer.profile.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ProfileViewModels {

    private ProfileViewModels() {
    }

    public record SkillView(String label, String originLabel) {
    }

    public record ExperienceView(String title, String company, LocalDate startDate,
                                 LocalDate endDate, String description) {
    }

    public record EducationView(String kindLabel, String label, String institution,
                                LocalDate obtainedOn) {
    }

    public record LanguageView(String language, String level) {
    }

    public record ProfileView(Long id, String fullName, String professionalTitle,
                              String referenceLocation, List<SkillView> skills,
                              List<ExperienceView> experiences, List<EducationView> educations,
                              List<LanguageView> languages, boolean cvAssisted, Instant updatedAt) {
        public boolean empty() {
            return (fullName == null || fullName.isBlank())
                    && (professionalTitle == null || professionalTitle.isBlank())
                    && (referenceLocation == null || referenceLocation.isBlank())
                    && skills.isEmpty() && experiences.isEmpty()
                    && educations.isEmpty() && languages.isEmpty();
        }
    }

    /**
     * Proposition issue du CV, portée par un formulaire de revue non persisté : elle ne devient
     * jamais du profil fiable avant validation explicite.
     */
    public record ProposalReview(com.hopeful117.cv_analyzer.profile.web.ProfileProposalForm form,
                                 String aiProvider, String aiModel) {
    }
}
