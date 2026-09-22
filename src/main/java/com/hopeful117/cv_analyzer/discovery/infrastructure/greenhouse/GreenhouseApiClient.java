package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class GreenhouseApiClient {

    private final RestTemplate restTemplate;
    private final GreenhouseProperties properties;

    public GreenhouseApiClient(RestTemplate restTemplate, GreenhouseProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public GreenhouseJobResponse fetchJobs(GreenhouseProperties.Board board) {
        String uri = UriComponentsBuilder.fromUriString(properties.baseUrl() + "/boards/{boardToken}/jobs")
                .queryParam("content", true)
                .buildAndExpand(board.boardToken())
                .toUriString();

        log.info("Fetching Greenhouse board jobs for company: {}", board.companyName());
        GreenhouseJobResponse response = restTemplate.getForObject(uri, GreenhouseJobResponse.class);
        return response == null ? new GreenhouseJobResponse(java.util.List.of(), new GreenhouseJobResponse.Meta(0)) : response;
    }
}
