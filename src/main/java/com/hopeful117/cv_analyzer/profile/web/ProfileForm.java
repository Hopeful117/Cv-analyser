package com.hopeful117.cv_analyzer.profile.web;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulaire d'édition manuelle du profil complet : l'état soumis remplace l'état existant.
 */
@Getter
@Setter
public class ProfileForm {

    @Size(max = 200)
    private String fullName;

    @Size(max = 200)
    private String professionalTitle;

    @Size(max = 300)
    private String referenceLocation;

    /** Une compétence par ligne. */
    @Size(max = 20_000)
    private String skillsText;

    /** Une langue par ligne, format « Langue : niveau » (niveau facultatif). */
    @Size(max = 5_000)
    private String languagesText;

    /** Une formation par ligne, format « Intitulé | Établissement | Date ». */
    @Size(max = 10_000)
    private String educationText;

    /** Une certification par ligne, format « Intitulé | Organisme | Date ». */
    @Size(max = 10_000)
    private String certificationText;

    private List<ExperienceLine> experiences = new ArrayList<>();

    @Getter
    @Setter
    public static class ExperienceLine {
        @Size(max = 200)
        private String title;
        @Size(max = 200)
        private String company;
        private LocalDate startDate;
        private LocalDate endDate;
        @Size(max = 2000)
        private String description;
    }
}
