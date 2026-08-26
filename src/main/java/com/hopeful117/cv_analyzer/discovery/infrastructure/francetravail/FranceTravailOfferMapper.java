package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import com.hopeful117.cv_analyzer.discovery.domain.JobOfferCompetency;
import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FranceTravailOfferMapper {

    private static final Pattern SALARY_PATTERN = Pattern.compile(
            "Annuel\\s+de\\s+(\\d+[.,]?\\d*)\\s+Euros(?:\\s+[aà]\\s+(\\d+[.,]?\\d*)\\s+Euros)?\\s+sur\\s+(\\d+\\.?\\d*)\\s+mois",
            Pattern.CASE_INSENSITIVE
    );

    private FranceTravailOfferMapper() {
    }

    public static JobOffer toDomain(FranceTravailOfferDto dto) {
        if (dto == null) return null;

        Instant now = Instant.now();
        List<JobOfferCompetency> competencies = mapCompetencies(dto.competences());
        WorkMode workMode = mapWorkMode(dto);

        return new JobOffer(
                "france-travail",
                dto.id(),
                dto.origineOffre() != null ? dto.origineOffre().urlOrigine() : null,
                now,
                dto.intitule(),
                dto.description(),
                dto.entreprise() != null ? dto.entreprise().nom() : null,
                dto.romeCode(),
                dto.romeLibelle(),
                dto.appellationlibelle(),
                dto.lieuTravail() != null ? dto.lieuTravail().libelle() : null,
                dto.lieuTravail() != null ? dto.lieuTravail().commune() : null,
                dto.lieuTravail() != null ? dto.lieuTravail().codePostal() : null,
                dto.lieuTravail() != null ? dto.lieuTravail().latitude() : null,
                dto.lieuTravail() != null ? dto.lieuTravail().longitude() : null,
                ContractType.fromCode(dto.typeContrat()),
                dto.typeContrat(),
                dto.typeContratLibelle(),
                competencies,
                dto.experienceLibelle(),
                workMode,
                dto.dureeTravailLibelle(),
                dto.salaire() != null ? dto.salaire().libelle() : null,
                parseSalaryMin(dto.salaire()),
                parseSalaryMax(dto.salaire()),
                parseSalaryPeriod(dto.salaire()),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

    private static List<JobOfferCompetency> mapCompetencies(List<FranceTravailOfferDto.FranceTravailCompetencyDto> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .map(d -> new JobOfferCompetency(d.code(), d.libelle(), d.exigence()))
                .toList();
    }

    private static WorkMode mapWorkMode(FranceTravailOfferDto dto) {
        return null;
    }

    private static BigDecimal parseSalaryMin(FranceTravailOfferDto.FranceTravailSalaryDto salary) {
        if (salary == null || salary.libelle() == null) return null;
        Matcher m = SALARY_PATTERN.matcher(salary.libelle());
        if (!m.find()) return null;
        String minStr = m.group(1).replace(',', '.');
        try {
            return new BigDecimal(minStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseSalaryMax(FranceTravailOfferDto.FranceTravailSalaryDto salary) {
        if (salary == null || salary.libelle() == null) return null;
        Matcher m = SALARY_PATTERN.matcher(salary.libelle());
        if (!m.find()) return null;
        String maxStr = m.group(2);
        if (maxStr == null) {
            maxStr = m.group(1);
        }
        maxStr = maxStr.replace(',', '.');
        try {
            return new BigDecimal(maxStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static SalaryPeriod parseSalaryPeriod(FranceTravailOfferDto.FranceTravailSalaryDto salary) {
        if (salary == null || salary.libelle() == null) return null;
        Matcher m = SALARY_PATTERN.matcher(salary.libelle());
        if (!m.find()) return null;
        String monthsStr = m.group(3);
        try {
            int months = (int) Double.parseDouble(monthsStr);
            return months >= 12 ? SalaryPeriod.ANNUAL : SalaryPeriod.MONTHLY;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
