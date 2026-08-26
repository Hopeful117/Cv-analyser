package com.hopeful117.cv_analyzer.discovery.domain;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record JobOffer(
        String providerKey,
        String providerOfferId,
        String originUrl,
        Instant fetchedAt,
        String title,
        String description,
        String company,
        String romeCode,
        String romeLabel,
        String appellationLabel,
        String locationLabel,
        String communeCode,
        String postalCode,
        BigDecimal latitude,
        BigDecimal longitude,
        ContractType canonicalContractType,
        String rawContractCode,
        String rawContractLabel,
        List<JobOfferCompetency> competencies,
        String experienceLabel,
        WorkMode workMode,
        String workDurationLabel,
        String rawSalaryText,
        BigDecimal salaryMinAmount,
        BigDecimal salaryMaxAmount,
        SalaryPeriod salaryPeriod,
        Instant providerCreatedAt,
        Instant providerUpdatedAt
) {
    public JobOffer {
        competencies = competencies == null ? Collections.emptyList() : Collections.unmodifiableList(competencies);
    }
}
