package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferProvider;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchRequest;
import com.hopeful117.cv_analyzer.discovery.application.port.JobOfferSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "france-travail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledFranceTravailJobOfferProvider implements JobOfferProvider {

    @Override
    public JobOfferSearchResult search(JobOfferSearchRequest request) {
        log.warn("France Travail integration is disabled. Cannot search for: {}", request.targetRole());
        throw new IllegalStateException(
                "L'intégration France Travail n'est pas configurée. " +
                "Activez france-travail.enabled=true et configurez les credentials."
        );
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
