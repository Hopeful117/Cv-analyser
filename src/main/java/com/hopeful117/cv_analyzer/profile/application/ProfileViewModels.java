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

    /** Reconstruit le formulaire d'édition manuelle depuis l'état fiable (mapping présentation). */
    public static com.hopeful117.cv_analyzer.profile.web.ProfileForm toManualForm(ProfileView view) {
        com.hopeful117.cv_analyzer.profile.web.ProfileForm form =
                new com.hopeful117.cv_analyzer.profile.web.ProfileForm();
        form.setFullName(view.fullName());
        form.setProfessionalTitle(view.professionalTitle());
        form.setReferenceLocation(view.referenceLocation());
        StringBuilder skills = new StringBuilder();
        view.skills().forEach(skill -> skills.append(skill.label()).append('\n'));
        form.setSkillsText(skills.toString());
        StringBuilder languages = new StringBuilder();
        view.languages().forEach(language -> {
            languages.append(language.language());
            if (language.level() != null && !language.level().isBlank()) {
                languages.append(" : ").append(language.level());
            }
            languages.append('\n');
        });
        form.setLanguagesText(languages.toString());
        StringBuilder education = new StringBuilder();
        StringBuilder certifications = new StringBuilder();
        view.educations().forEach(item -> {
            StringBuilder line = new StringBuilder(item.label());
            if (item.institution() != null) {
                line.append(" | ").append(item.institution());
            }
            if (item.obtainedOn() != null) {
                line.append(" | ").append(item.obtainedOn().getYear());
            }
            line.append('\n');
            if ("Certification".equals(item.kindLabel())) {
                certifications.append(line);
            } else {
                education.append(line);
            }
        });
        form.setEducationText(education.toString());
        form.setCertificationText(certifications.toString());
        view.experiences().forEach(experience -> {
            com.hopeful117.cv_analyzer.profile.web.ProfileForm.ExperienceLine line =
                    new com.hopeful117.cv_analyzer.profile.web.ProfileForm.ExperienceLine();
            line.setTitle(experience.title());
            line.setCompany(experience.company());
            line.setStartDate(experience.startDate());
            line.setEndDate(experience.endDate());
            line.setDescription(experience.description());
            form.getExperiences().add(line);
        });
        padExperienceRows(form);
        return form;
    }

    public static com.hopeful117.cv_analyzer.profile.web.ProfileForm emptyManualForm() {
        com.hopeful117.cv_analyzer.profile.web.ProfileForm form =
                new com.hopeful117.cv_analyzer.profile.web.ProfileForm();
        padExperienceRows(form);
        return form;
    }

    private static void padExperienceRows(com.hopeful117.cv_analyzer.profile.web.ProfileForm form) {
        while (form.getExperiences().size() < 2) {
            form.getExperiences().add(new com.hopeful117.cv_analyzer.profile.web.ProfileForm.ExperienceLine());
        }
    }
}
