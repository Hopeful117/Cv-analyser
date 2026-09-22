package com.hopeful117.cv_analyzer.discovery.infrastructure.adzuna;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class AdzunaOfferMapper {

    private AdzunaOfferMapper() {
    }

    public static JobOffer toDomain(AdzunaSearchResponse.AdzunaOfferDto dto) {
        if (dto == null) return null;

        return new JobOffer(
                "adzuna",
                dto.id(),
                dto.redirect_url(),
                Instant.now(),
                dto.title(),
                dto.description(),
                dto.company() != null ? dto.company().display_name() : null,
                null,
                dto.category() != null ? dto.category().label() : null,
                null,
                dto.location() != null ? dto.location().display_name() : null,
                null,
                null,
                dto.latitude(),
                dto.longitude(),
                mapContractType(dto.contract_type()),
                dto.contract_type(),
                dto.contract_time(),
                List.of(),
                null,
                (WorkMode) null,
                null,
                salaryText(dto),
                dto.salary_min(),
                dto.salary_max(),
                null,
                parseInstant(dto.created()),
                null
        );
    }

    private static ContractType mapContractType(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase()) {
            case "permanent" -> ContractType.CDI;
            case "contract" -> ContractType.CDD;
            case "temporary" -> ContractType.INTERIM;
            case "internship" -> ContractType.STAGE;
            case "freelance" -> ContractType.FREELANCE;
            default -> null;
        };
    }

    private static String salaryText(AdzunaSearchResponse.AdzunaOfferDto dto) {
        if (dto.salary_min() == null && dto.salary_max() == null) return null;
        if (dto.salary_min() != null && dto.salary_max() != null
                && dto.salary_min().compareTo(dto.salary_max()) != 0) {
            return dto.salary_min() + " - " + dto.salary_max();
        }
        return dto.salary_min() != null ? dto.salary_min().toString() : dto.salary_max().toString();
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }
}
