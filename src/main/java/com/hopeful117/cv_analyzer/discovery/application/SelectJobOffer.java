package com.hopeful117.cv_analyzer.discovery.application;

import com.hopeful117.cv_analyzer.career.application.OpportunityCreationRequest;
import com.hopeful117.cv_analyzer.career.application.OpportunityService;
import com.hopeful117.cv_analyzer.career.domain.OpportunitySourceType;
import com.hopeful117.cv_analyzer.career.domain.OpportunityStatus;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class SelectJobOffer {

    private final OpportunityService opportunityService;

    @Transactional
    public Long select(JobOffer offer) {
        String safeUrl = safeHttpUrl(offer.originUrl());
        return opportunityService.create(new OpportunityCreationRequest(
                null,
                offer.title(),
                offer.company(),
                offer.canonicalContractType(),
                offer.rawContractCode(),
                null,
                null,
                null,
                offer.providerKey(),
                offer.rawSalaryText(),
                null,
                offer.locationLabel(),
                safeUrl == null ? OpportunitySourceType.MANUAL : OpportunitySourceType.URL,
                safeUrl,
                "manual".equalsIgnoreCase(offer.providerKey()) ? offer.description() : "",
                "manual".equalsIgnoreCase(offer.providerKey()) ? offer.description() : "",
                null,
                OpportunityStatus.DRAFT
        )).getId();
    }

    private static String safeHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    ? value.trim() : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
