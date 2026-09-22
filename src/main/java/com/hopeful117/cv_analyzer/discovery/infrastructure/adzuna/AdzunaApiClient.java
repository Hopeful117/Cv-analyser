package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class AdzunaApiClient {

    private final RestTemplate restTemplate;
    private final AdzunaProperties properties;

    public AdzunaApiClient(RestTemplate restTemplate, AdzunaProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public AdzunaSearchResponse search(String keywords, int maxResults) {
        String uri = UriComponentsBuilder.fromUriString(properties.baseUrl() + "/1")
                .queryParam("app_id", properties.appId())
                .queryParam("app_key", properties.appKey())
                .queryParam("what", keywords)
                .queryParam("results_per_page", Math.min(maxResults, 50))
                .toUriString();

        log.info("Searching Adzuna offers: keywords='{}', maxResults={}", keywords, maxResults);
        ResponseEntity<AdzunaSearchResponse> response = restTemplate.getForEntity(uri, AdzunaSearchResponse.class);
        AdzunaSearchResponse body = response.getBody();
        return body == null ? new AdzunaSearchResponse(0, List.of()) : body;
    }
}
