package com.hopeful117.cv_analyzer.discovery.domain;

public record JobOfferCompetency(
        String code,
        String label,
        String requirement
) {
    public boolean isRequired() {
        return "E".equals(requirement);
    }

    public boolean isDesired() {
        return "S".equals(requirement);
    }
}
