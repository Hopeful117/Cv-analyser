package com.hopeful117.cv_analyzer.career.application.consultation;

import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetHeaderResolver;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompareGoogleSheetApplicationUseCase {
    private static final Set<String> IGNORED = Set.of(
            GoogleSheetHeaderResolver.canonicalName("ID"),
            GoogleSheetHeaderResolver.canonicalName("Jours"),
            GoogleSheetHeaderResolver.canonicalName("Dernière synchronisation"),
            GoogleSheetHeaderResolver.canonicalName("Statut synchronisation"));
    public GoogleSheetComparisonResult compare(GoogleSheetApplicationRow row,
                                               ApplicationSheetProjection crm,
                                               boolean duplicateExternalId) {
        Long crmId = crm == null ? null : parseCrmId(crm.displayId());
        if (!row.valid()) {
            return result(GoogleSheetComparisonState.INVALID, row, crm, crmId, List.of());
        }
        if (duplicateExternalId) {
            return result(GoogleSheetComparisonState.DUPLICATE_EXTERNAL_ID,
                    row, crm, crmId, List.of());
        }
        if (crm == null) {
            return result(GoogleSheetComparisonState.MISSING_IN_CRM,
                    row, null, null, List.of());
        }

        Map<String, String> sheetValues = canonicalize(row.columns());
        Map<String, String> expectedValues = crmColumns(crm);
        List<GoogleSheetDifference> differences = new ArrayList<>();
        expectedValues.forEach((displayName, expected) -> {
            String canonical = GoogleSheetHeaderResolver.canonicalName(displayName);
            if (IGNORED.contains(canonical) || !sheetValues.containsKey(canonical)) return;
            String actual = sheetValues.get(canonical);
            String expectedText = Objects.toString(expected, "");
            if (!equivalent(actual, expectedText)) {
                differences.add(new GoogleSheetDifference(displayName,
                        emptyLabel(actual), emptyLabel(expectedText)));
            }
        });
        return result(differences.isEmpty() ? GoogleSheetComparisonState.SYNCHRONIZED
                        : GoogleSheetComparisonState.DIFFERENT,
                row, crm, crmId, List.copyOf(differences));
    }

    public Map<String, String> crmColumns(ApplicationSheetProjection p) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("ID", text(p.displayId()));
        row.put("Entreprise", text(p.companyName()));
        row.put("Ville", text(p.city()));
        row.put("Adresse", text(p.address()));
        row.put("Téléphone", text(p.phone()));
        row.put("Email", text(p.email()));
        row.put("Site", text(p.website()));
        row.put("Poste", text(p.jobTitle()));
        row.put("Lien offre", text(p.offerUrl()));
        row.put("Contrat", text(p.contractType()));
        row.put("Temps", text(p.workSchedule()));
        row.put("Télétravail", text(p.remoteMode()));
        row.put("Source", text(p.source()));
        row.put("CV envoyé", p.resumeSent() ? "Oui" : "Non");
        row.put("LM envoyée", p.coverLetterSent() ? "Oui" : "Non");
        row.put("Portfolio", p.portfolioSent() ? "Oui" : "Non");
        row.put("Date candidature", p.appliedAt() == null ? "" :
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(p.appliedAt()));
        row.put("Relance prévue", p.followUpPlannedAt() == null ? "" :
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(p.followUpPlannedAt()));
        row.put("Dernière relance", p.lastFollowUpAt() == null ? "" :
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(p.lastFollowUpAt()));
        row.put("Statut", text(p.status()));
        row.put("Entretien", text(p.interview()));
        row.put("Décision", text(p.decision()));
        row.put("Salaire", text(p.salary()));
        row.put("Distance", text(p.distance()));
        row.put("Priorité", text(p.priority()));
        row.put("Notes", text(p.notes()));
        row.put("Career Intelligence ID", text(p.careerIntelligenceId()));
        row.put("Score IA", p.aiScore() == null ? "" : String.valueOf(p.aiScore()));
        row.put("Version CV", p.resumeVersion() == null ? "" : String.valueOf(p.resumeVersion()));
        return Map.copyOf(row);
    }

    public GoogleSheetComparisonResult missingInSheet(ApplicationSheetProjection crm) {
        return result(GoogleSheetComparisonState.MISSING_IN_SHEET, null, crm,
                parseCrmId(crm.displayId()), List.of());
    }

    private GoogleSheetComparisonResult result(GoogleSheetComparisonState state,
                                               GoogleSheetApplicationRow row,
                                               ApplicationSheetProjection crm, Long crmId,
                                               List<GoogleSheetDifference> differences) {
        return new GoogleSheetComparisonResult(state, row, crm, crmId, differences);
    }

    private Map<String, String> canonicalize(Map<String, String> columns) {
        Map<String, String> result = new HashMap<>();
        columns.forEach((key, value) ->
                result.put(GoogleSheetHeaderResolver.canonicalName(key), value));
        return result;
    }

    private boolean equivalent(String left, String right) {
        return normalize(left).equals(normalize(right));
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (normalized.startsWith("'") && normalized.length() > 1 &&
                "=+-@".indexOf(normalized.charAt(1)) >= 0) {
            normalized = normalized.substring(1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String emptyLabel(String value) {
        return value == null || value.isBlank() ? "Non renseigné" : value;
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private Long parseCrmId(String displayId) {
        try {
            return Long.valueOf(displayId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
