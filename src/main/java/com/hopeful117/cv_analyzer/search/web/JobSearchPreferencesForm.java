package com.hopeful117.cv_analyzer.search.web;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * État complet des préférences de recherche : la soumission remplace l'état actif existant.
 * Aucune valeur n'est déduite du profil ou d'ailleurs : tout vient de cette saisie utilisateur.
 */
@Getter
@Setter
public class JobSearchPreferencesForm {

    /** Un intitulé de poste visé par ligne. */
    @Size(max = 5_000)
    private String targetRolesText;

    /** Un lieu par ligne (ville, région ou pays). */
    @Size(max = 5_000)
    private String locationsText;

    /** Une technologie recherchée par ligne (critère de pertinence). */
    @Size(max = 5_000)
    private String preferredTechnologiesText;

    /** Une technologie exclue par ligne (critère éliminatoire). */
    @Size(max = 5_000)
    private String excludedTechnologiesText;

    /** Vide : ouvert à tous les modes de travail. */
    private Set<WorkMode> workModes = new LinkedHashSet<>();

    /** Vide : aucune restriction de contrat. */
    private Set<ContractType> contractTypes = new LinkedHashSet<>();

    private boolean openToRelocation;

    @Size(max = 3)
    private String salaryCurrency;

    private SalaryPeriod salaryPeriod;
    private Integer salaryMinAmount;

    public boolean empty() {
        return blank(targetRolesText) && blank(locationsText)
                && blank(preferredTechnologiesText) && blank(excludedTechnologiesText)
                && workModes.isEmpty() && contractTypes.isEmpty()
                && !openToRelocation && salaryMinAmount == null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
