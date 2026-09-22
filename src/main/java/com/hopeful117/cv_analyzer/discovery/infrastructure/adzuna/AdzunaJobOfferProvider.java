package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchRequest;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class AdzunaJobOfferProvider implements JobOfferProvider {

    private final AdzunaApiClient apiClient;
    private final AdzunaProperties properties;

    public AdzunaJobOfferProvider(AdzunaApiClient apiClient, AdzunaProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties;
    }

    @Override
    public JobOfferSearchResult search(JobOfferSearchRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("L'intégration Adzuna n'est pas configurée.");
        }

        AdzunaSearchResponse response = apiClient.search(request.targetRole(), request.maxResults());
        List<JobOffer> offers = response.results() == null ? List.of() : response.results().stream()
                .map(AdzunaOfferMapper::toDomain)
                .toList();
        return new JobOfferSearchResult(
                offers,
                offers.size(),
                response.count() != null ? response.count() : offers.size(),
                request.targetRole(),
                "adzuna",
                Instant.now()
        );
    }

    @Override
    public boolean isAvailable() {
        return properties.configured();
    }
}
