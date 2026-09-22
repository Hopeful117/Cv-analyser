package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdzunaSearchResponse(
        Integer count,
        List<AdzunaOfferDto> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdzunaOfferDto(
            String id,
            String title,
            String description,
            String redirect_url,
            String created,
            AdzunaLocation location,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal salary_min,
            BigDecimal salary_max,
            String contract_type,
            String contract_time,
            AdzunaCompany company,
            AdzunaCategory category
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdzunaLocation(String display_name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdzunaCompany(String display_name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdzunaCategory(String label, String tag) {
    }
}
