package com.hopeful117.cv_analyzer.profile.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * État de revue d'une proposition issue du CV : chaque valeur est appliquée uniquement si sa case
 * « appliquer » est cochée, après édition éventuelle. Ce formulaire n'est pas persisté : tant qu'il
 * n'est pas validé, le profil fiable reste inchangé.
 */
@Getter
@Setter
public class ProfileProposalForm {

    private boolean applyFullName;
    @Size(max = 200)
    private String fullName;

    private boolean applyProfessionalTitle;
    @Size(max = 200)
    private String professionalTitle;

    private boolean applyReferenceLocation;
    @Size(max = 300)
    private String referenceLocation;

    private List<@Valid SkillEntry> skills = new ArrayList<>();
    private List<@Valid ExperienceEntry> experiences = new ArrayList<>();
    private List<@Valid EducationEntry> educations = new ArrayList<>();
    private List<@Valid LanguageEntry> languages = new ArrayList<>();

    /** Traçabilité de la proposition, renvoyée par l'écran de revue. */
    private String aiProvider;
    private String aiModel;

    public boolean nothingSelected() {
        return !applyFullName && !applyProfessionalTitle && !applyReferenceLocation
                && skills.stream().noneMatch(SkillEntry::isApply)
                && experiences.stream().noneMatch(ExperienceEntry::isApply)
                && educations.stream().noneMatch(EducationEntry::isApply)
                && languages.stream().noneMatch(LanguageEntry::isApply);
    }

    @Getter
    @Setter
    public static class SkillEntry {
        private boolean apply;
        @Size(max = 120)
        private String label;
    }

    @Getter
    @Setter
    public static class ExperienceEntry {
        private boolean apply;
        @Size(max = 200)
        private String title;
        @Size(max = 200)
        private String company;
        private LocalDate startDate;
        private LocalDate endDate;
        @Size(max = 2000)
        private String description;
    }

    @Getter
    @Setter
    public static class EducationEntry {
        private boolean apply;
        private String kind;
        @Size(max = 255)
        private String label;
        @Size(max = 200)
        private String institution;
        private LocalDate obtainedOn;
    }

    @Getter
    @Setter
    public static class LanguageEntry {
        private boolean apply;
        @Size(max = 60)
        private String language;
        @Size(max = 40)
        private String level;
    }
}
