package com.hopeful117.cv_analyzer.discovery.application.port;

import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;

import java.util.List;

public record JobOfferSearchRequest(
        String targetRole,
        int maxResults
) {
    public static JobOfferSearchRequest of(String targetRole) {
        return new JobOfferSearchRequest(targetRole, 50);
    }
}
