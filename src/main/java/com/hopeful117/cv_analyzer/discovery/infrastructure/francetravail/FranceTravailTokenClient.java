package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class FranceTravailTokenClient {

    private final RestTemplate restTemplate;
    private final FranceTravailProperties properties;

    private String cachedToken;
    private Instant tokenExpiration;

    public FranceTravailTokenClient(RestTemplate restTemplate, FranceTravailProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && tokenExpiration != null && Instant.now().isBefore(tokenExpiration)) {
            return cachedToken;
        }

        String scope = "api_offresdemploiv2 o2dsoffre application_" + properties.clientId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", properties.clientId());
        body.add("client_secret", properties.clientSecret());
        body.add("scope", scope);

        log.info("Requesting new France Travail access token for client {}", properties.maskedClientId());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                properties.tokenUrl(),
                request,
                Map.class
        );

        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            log.error("Failed to obtain France Travail access token: {}", responseBody);
            throw new FranceTravailAuthenticationException("Impossible d'obtenir le token d'accès");
        }

        cachedToken = (String) responseBody.get("access_token");
        Object expiresInObj = responseBody.get("expires_in");
        int expiresIn = expiresInObj instanceof Integer ? (Integer) expiresInObj : 1499;
        tokenExpiration = Instant.now().plusSeconds(expiresIn - 60);

        log.info("France Travail access token obtained, expires in {} seconds", expiresIn);
        return cachedToken;
    }

    public synchronized void invalidateToken() {
        cachedToken = null;
        tokenExpiration = null;
    }
}
