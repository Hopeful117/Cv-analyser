package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Slf4j
@Component
public class FranceTravailApiClient {

    private final RestTemplate restTemplate;
    private final FranceTravailTokenClient tokenClient;
    private final FranceTravailProperties properties;

    public FranceTravailApiClient(RestTemplate restTemplate, FranceTravailTokenClient tokenClient,
                                  FranceTravailProperties properties) {
        this.restTemplate = restTemplate;
        this.tokenClient = tokenClient;
        this.properties = properties;
    }

    public FranceTravailSearchResponse search(String keywords, int maxResults) {
        String token = tokenClient.getAccessToken();

        String uri = UriComponentsBuilder.fromUriString(properties.baseUrl() + "/offres/search")
                .queryParam("motsCles", keywords)
                .queryParam("range", "0-" + (maxResults - 1))
                .toUriString();

        log.info("Searching France Travail offers: keywords='{}', range=0-{}", keywords, maxResults - 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FranceTravailSearchResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                FranceTravailSearchResponse.class
        );

        FranceTravailSearchResponse body = response.getBody();
        if (body == null) {
            log.warn("Empty response from France Travail search");
            return new FranceTravailSearchResponse(Collections.emptyList(), 0, null);
        }

        log.info("France Travail search returned {} offers", body.resultats() != null ? body.resultats().size() : 0);
        return body;
    }
}
