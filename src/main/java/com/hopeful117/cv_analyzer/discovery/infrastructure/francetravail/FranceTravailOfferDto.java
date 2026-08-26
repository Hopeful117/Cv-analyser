package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FranceTravailOfferDto(
        String id,
        String intitule,
        String description,
        @JsonProperty("dateCreation") String dateCreationRaw,
        @JsonProperty("dateActualisation") String dateActualisationRaw,
        FranceTravailLocationDto lieuTravail,
        String romeCode,
        String romeLibelle,
        String appellationlibelle,
        FranceTravailCompanyDto entreprise,
        String typeContrat,
        String typeContratLibelle,
        String natureContrat,
        String experienceExige,
        String experienceLibelle,
        List<FranceTravailCompetencyDto> competences,
        FranceTravailSalaryDto salaire,
        String dureeTravailLibelle,
        String dureeTravailLibelleConverti,
        Boolean alternance,
        FranceTravailOriginDto origineOffre,
        Boolean entrepriseAdaptee,
        Boolean employeurHandiEngage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranceTravailLocationDto(
            String libelle,
            BigDecimal latitude,
            BigDecimal longitude,
            String codePostal,
            String commune
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranceTravailCompanyDto(
            String nom
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranceTravailCompetencyDto(
            String code,
            String libelle,
            String exigence
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranceTravailSalaryDto(
            String libelle,
            @JsonProperty("complement1") String complement1,
            @JsonProperty("complement2") String complement2
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FranceTravailOriginDto(
            String origine,
            @JsonProperty("urlOrigine") String urlOrigine
    ) {}

    public Instant getCreatedAt() {
        return parseInstant(dateCreationRaw);
    }

    public Instant getUpdatedAt() {
        return parseInstant(dateActualisationRaw);
    }

    private Instant parseInstant(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return Instant.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }
}
