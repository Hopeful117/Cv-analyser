package com.hopeful117.cv_analyzer.profile.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrat de sortie IA : une PROPOSITION de profil structurée, jamais persistée telle quelle.
 * Les dates restent des chaînes : elles sont parsées de façon défensive côté applicatif.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedProfileProposal {
    private String fullName;
    private String professionalTitle;
    private String location;
    private List<ProposedSkill> skills = new ArrayList<>();
    private List<ProposedExperience> experiences = new ArrayList<>();
    private List<ProposedEducation> educations = new ArrayList<>();
    private List<ProposedLanguage> languages = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedSkill {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedExperience {
        private String title;
        private String company;
        private String startDate;
        private String endDate;
        private String summary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedEducation {
        private String kind;
        private String label;
        private String institution;
        private String obtainedOn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedLanguage {
        private String language;
        private String level;
    }
}
