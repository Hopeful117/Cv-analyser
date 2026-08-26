package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FranceTravailSearchResponse(
        List<FranceTravailOfferDto> resultats,
        @JsonProperty("nbResultats") Integer nbResultats,
        @JsonProperty("nbPages") Integer nbPages
) {
}
