package com.hopeful117.cv_analyzer.discovery.domain;

import com.hopeful117.cv_analyzer.search.domain.SalaryPeriod;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class EligibilityEvaluator {

    private EligibilityEvaluator() {
    }

    public static EligibilityResult evaluate(JobOffer offer, JobSearchPreferencesEntity preferences) {
        List<EligibilityReason> reasons = new ArrayList<>();
        boolean hasRejection = false;
        boolean hasUnresolved = false;

        // 1. Contract check
        if (preferences.getContractTypes() != null && !preferences.getContractTypes().isEmpty()) {
            if (offer.canonicalContractType() == null) {
                reasons.add(EligibilityReason.of(
                        EligibilityReasonType.CONTRACT_UNKNOWN,
                        "Contrat fournisseur inconnu : " + offer.rawContractCode()));
                hasUnresolved = true;
            } else if (!preferences.getContractTypes().contains(offer.canonicalContractType())) {
                reasons.add(EligibilityReason.of(
                        EligibilityReasonType.CONTRACT_NOT_ACCEPTED,
                        "Contrat " + offer.canonicalContractType() + " non accepté"));
                hasRejection = true;
            }
        }

        // 2. Work mode check
        if (preferences.getAcceptedWorkModes() != null && !preferences.getAcceptedWorkModes().isEmpty()) {
            if (offer.workMode() == null) {
                reasons.add(EligibilityReason.of(
                        EligibilityReasonType.WORK_MODE_UNKNOWN,
                        "Mode de travail non renseigné par le fournisseur"));
                hasUnresolved = true;
            } else if (!preferences.getAcceptedWorkModes().contains(offer.workMode())) {
                reasons.add(EligibilityReason.of(
                        EligibilityReasonType.WORK_MODE_NOT_ACCEPTED,
                        "Mode de travail " + offer.workMode().getFrenchLabel() + " non accepté"));
                hasRejection = true;
            }
        }

        // 3. Excluded technologies check
        if (preferences.getTechnologies() != null) {
            List<String> excludedNames = preferences.getTechnologies().stream()
                    .filter(t -> t.getKind() == com.hopeful117.cv_analyzer.search.domain.TechnologyPreference.EXCLUDED)
                    .map(t -> t.getNormalizedName().toLowerCase())
                    .toList();

            if (!excludedNames.isEmpty() && offer.competencies() != null) {
                for (JobOfferCompetency competency : offer.competencies()) {
                    String normalizedLabel = normalize(competency.label());
                    if (excludedNames.contains(normalizedLabel)) {
                        reasons.add(EligibilityReason.of(
                                EligibilityReasonType.EXCLUDED_TECHNOLOGY_FOUND,
                                "Technologie exclue détectée : " + competency.label()));
                        hasRejection = true;
                        break;
                    }
                }
            }
        }

        // 4. Salary check
        if (preferences.getSalaryMinAmount() != null && preferences.getSalaryMinAmount() > 0) {
            if (offer.salaryMinAmount() == null) {
                reasons.add(EligibilityReason.of(
                        EligibilityReasonType.SALARY_UNKNOWN,
                        "Salaire non renseigné"));
                hasUnresolved = true;
            } else if (offer.salaryPeriod() != null && preferences.getSalaryPeriod() != null) {
                BigDecimal offerAnnual = toAnnual(offer.salaryMinAmount(), offer.salaryPeriod());
                BigDecimal prefAnnual = toAnnual(BigDecimal.valueOf(preferences.getSalaryMinAmount()), preferences.getSalaryPeriod());
                if (offerAnnual.compareTo(prefAnnual) < 0) {
                    reasons.add(EligibilityReason.of(
                            EligibilityReasonType.SALARY_BELOW_MINIMUM,
                            "Salaire minimum estimé " + offerAnnual + " EUR/an inférieur au seuil " + prefAnnual + " EUR/an"));
                    hasRejection = true;
                }
            }
        }

        // 5. Preferred technologies (positive evidence only)
        if (preferences.getTechnologies() != null) {
            List<String> preferredNames = preferences.getTechnologies().stream()
                    .filter(t -> t.getKind() == com.hopeful117.cv_analyzer.search.domain.TechnologyPreference.PREFERRED)
                    .map(t -> t.getNormalizedName().toLowerCase())
                    .toList();

            if (!preferredNames.isEmpty() && offer.competencies() != null) {
                for (JobOfferCompetency competency : offer.competencies()) {
                    String normalizedLabel = normalize(competency.label());
                    if (preferredNames.contains(normalizedLabel)) {
                        reasons.add(EligibilityReason.of(
                                EligibilityReasonType.PREFERRED_TECHNOLOGY_FOUND,
                                "Technologie recherchée détectée : " + competency.label()));
                    }
                }
            }
        }

        // Determine final status
        if (hasRejection) {
            return EligibilityResult.of(EligibilityStatus.INELIGIBLE, reasons);
        }
        if (hasUnresolved) {
            return EligibilityResult.of(EligibilityStatus.REVIEW_REQUIRED, reasons);
        }
        return EligibilityResult.of(EligibilityStatus.ELIGIBLE, reasons);
    }

    private static String normalize(String label) {
        if (label == null) return "";
        return label.trim().toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[ôö]", "o")
                .replaceAll("[îï]", "i")
                .replaceAll("[ç]", "c");
    }

    private static BigDecimal toAnnual(BigDecimal amount, SalaryPeriod period) {
        return switch (period) {
            case ANNUAL -> amount;
            case MONTHLY -> amount.multiply(BigDecimal.valueOf(12));
        };
    }
}
