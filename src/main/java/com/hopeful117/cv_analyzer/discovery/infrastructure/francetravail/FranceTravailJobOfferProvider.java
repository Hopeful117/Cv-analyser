package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchRequest;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "france-travail", name = "enabled", havingValue = "true")
public class FranceTravailJobOfferProvider implements JobOfferProvider {

    private final FranceTravailApiClient apiClient;

    public FranceTravailJobOfferProvider(FranceTravailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public JobOfferSearchResult search(JobOfferSearchRequest request) {
        log.info("Searching France Travail for role: {}", request.targetRole());

        FranceTravailSearchResponse response = apiClient.search(request.targetRole(), request.maxResults());

        List<JobOffer> offers = response.resultats() != null
                ? response.resultats().stream()
                    .map(FranceTravailOfferMapper::toDomain)
                    .toList()
                : Collections.emptyList();

        int totalAvailable = response.nbResultats() != null ? response.nbResultats() : offers.size();

        return new JobOfferSearchResult(
                offers,
                offers.size(),
                totalAvailable,
                request.targetRole(),
                "france-travail",
                Instant.now()
        );
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
