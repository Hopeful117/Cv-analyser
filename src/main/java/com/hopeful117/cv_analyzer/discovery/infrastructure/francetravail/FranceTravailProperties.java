package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "france-travail")
public record FranceTravailProperties(
        boolean enabled,
        String clientId,
        String clientSecret,
        String tokenUrl,
        String baseUrl
) {
    public FranceTravailProperties {
        tokenUrl = tokenUrl == null || tokenUrl.isBlank()
                ? "https://entreprise.francetravail.fr/connexion/oauth2/access_token?realm=%2Fpartenaire"
                : tokenUrl;
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.francetravail.io/partenaire/offresdemploi/v2"
                : baseUrl;
    }

    public boolean configured() {
        return enabled && clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public String maskedClientId() {
        if (clientId == null || clientId.length() < 10) return "****";
        return clientId.substring(0, 6) + "..." + clientId.substring(clientId.length() - 4);
    }
}
