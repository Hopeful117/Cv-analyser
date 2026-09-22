package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adzuna")
public record AdzunaProperties(
        boolean enabled,
        String appId,
        String appKey,
        String baseUrl
) {
    public AdzunaProperties {
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.adzuna.com/v1/api/jobs/fr/search"
                : baseUrl;
    }

    public boolean configured() {
        return enabled && appId != null && !appId.isBlank()
                && appKey != null && !appKey.isBlank();
    }
}
