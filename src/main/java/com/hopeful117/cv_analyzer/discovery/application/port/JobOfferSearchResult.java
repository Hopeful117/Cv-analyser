package com.hopeful117.cv_analyzer.discovery.application.port;

import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;

import java.time.Instant;
import java.util.List;

public record JobOfferSearchResult(
        List<JobOffer> offers,
        int returnedCount,
        int totalAvailable,
        String targetRole,
        String providerKey,
        Instant fetchedAt
) {
}
