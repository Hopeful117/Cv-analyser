package com.hopeful117.cv_analyzer.career.application.consultation;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionService;
import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsConsultationPort;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetHeaderResolver;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportGoogleSheetApplicationUseCase {
    private final GoogleSheetsConsultationPort consultationPort;
    private final ApplicationCrmService crmService;
    private final ApplicationProjectionService projectionService;

    public Preview preview(int rowNumber) {
        GoogleSheetApplicationRow row = requireUnlinkedRow(rowNumber);
        List<String> warnings = new ArrayList<>();
        ApplicationForm form = map(row, warnings);
        if (blank(form.getJobTitle())) {
            warnings.add("Le poste est absent du Sheet et doit être complété avant l’import.");
        }
        return new Preview(row, form, List.copyOf(warnings));
    }

    public long importRow(int rowNumber, ApplicationForm form) {
        GoogleSheetApplicationRow row = requireUnlinkedRow(rowNumber);
        if (crmService.isPotentialDuplicate(null, form.getCompanyName(),
                form.getJobTitle(), form.getAppliedAt())) {
            throw new IllegalArgumentException(
                    "Une candidature similaire existe déjà dans le CRM. Import annulé.");
        }
        long applicationId = crmService.createImportedFromGoogleSheet(form, row.rowNumber());
        projectionService.synchronize(applicationId);
        return applicationId;
    }

    private GoogleSheetApplicationRow requireUnlinkedRow(int rowNumber) {
        GoogleSheetApplicationRow row = consultationPort.readApplications().snapshot().rows().stream()
                .filter(candidate -> candidate.rowNumber() == rowNumber)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cette ligne Google Sheets est introuvable."));
        if (row.careerIntelligenceId() != null && !row.careerIntelligenceId().isBlank()) {
            throw new IllegalArgumentException(
                    "Cette ligne Google Sheets est déjà liée à Career Intelligence.");
        }
        if (blank(row.companyName()) || blank(row.status())) {
            throw new IllegalArgumentException(
                    "Cette ligne ne contient pas assez d’informations pour être importée.");
        }
        return row;
    }

    private ApplicationForm map(GoogleSheetApplicationRow row, List<String> warnings) {
        ApplicationForm form = new ApplicationForm();
        form.setCompanyName(value(row, "Entreprise"));
        form.setCity(value(row, "Ville"));
        form.setAddress(value(row, "Adresse"));
        form.setPhone(value(row, "Téléphone"));
        form.setEmail(optionalEmail(value(row, "Email")));
        form.setWebsite(url(value(row, "Site"), "site", warnings));
        form.setJobTitle(value(row, "Poste"));
        form.setOfferUrl(url(value(row, "Lien offre"), "lien de l’offre", warnings));
        form.setContractTypeRaw(value(row, "Contrat"));
        form.setWorkScheduleRaw(value(row, "Temps"));
        form.setRemoteMode(remote(value(row, "Télétravail")));
        form.setSource(value(row, "Source"));
        form.setPortfolioSent(yes(value(row, "Portfolio")));
        LocalDate appliedAt = date(value(row, "Date candidature"), warnings);
        if (appliedAt == null) {
            appliedAt = date(value(row, "CV envoyé"), warnings);
        }
        form.setAppliedAt(appliedAt);
        form.setFollowUpPlannedAt(date(value(row, "Relance prévue"), warnings));
        form.setLastFollowUpAt(date(value(row, "Dernière relance"), warnings));
        form.setStatus(status(value(row, "Statut"), warnings));
        form.setInterviewStatus(interview(value(row, "Entretien")));
        form.setDecision(decision(value(row, "Décision")));
        form.setSalaryText(value(row, "Salaire"));
        form.setDistanceText(value(row, "Distance"));
        form.setPriority(priority(value(row, "Priorité"), warnings));
        form.setNotes(value(row, "Notes"));
        return form;
    }

    private String value(GoogleSheetApplicationRow row, String name) {
        String canonical = GoogleSheetHeaderResolver.canonicalName(name);
        return row.columns().entrySet().stream()
                .filter(entry -> GoogleSheetHeaderResolver.canonicalName(entry.getKey())
                        .equals(canonical))
                .map(Map.Entry::getValue).filter(value -> !blank(value))
                .findFirst().orElse(null);
    }

    private ApplicationStatus status(String value, List<String> warnings) {
        return switch (GoogleSheetHeaderResolver.normalize(value)) {
            case "candidature envoyee", "postule" -> ApplicationStatus.APPLIED;
            case "en attente" -> ApplicationStatus.WAITING;
            case "entretien" -> ApplicationStatus.INTERVIEW;
            case "relance effectuee", "relance" -> ApplicationStatus.FOLLOWED_UP;
            case "refus", "refuse" -> ApplicationStatus.REJECTED;
            case "succes", "accepte" -> ApplicationStatus.SUCCESS;
            case "archive" -> ApplicationStatus.ARCHIVED;
            case "non demarche", "" -> ApplicationStatus.NOT_CONTACTED;
            default -> {
                warnings.add("Statut inconnu : « " + value + " ». Vérifiez la valeur proposée.");
                yield ApplicationStatus.NOT_CONTACTED;
            }
        };
    }

    private ApplicationPriority priority(String value, List<String> warnings) {
        return switch (GoogleSheetHeaderResolver.normalize(value)) {
            case "faible", "low" -> ApplicationPriority.LOW;
            case "haute", "high" -> ApplicationPriority.HIGH;
            case "moyenne", "medium", "" -> ApplicationPriority.MEDIUM;
            default -> {
                warnings.add("Priorité inconnue : « " + value + " ». Moyenne proposée.");
                yield ApplicationPriority.MEDIUM;
            }
        };
    }

    private RemoteMode remote(String value) {
        String normalized = GoogleSheetHeaderResolver.normalize(value);
        if (normalized.contains("hybrid") || normalized.contains("hybride")) return RemoteMode.HYBRID;
        if (normalized.contains("remote") || normalized.contains("distance")
                || normalized.equals("oui")) return RemoteMode.REMOTE;
        if (normalized.contains("site") || normalized.contains("presentiel")
                || normalized.equals("non")) return RemoteMode.ONSITE;
        return RemoteMode.UNSPECIFIED;
    }

    private InterviewStatus interview(String value) {
        String normalized = GoogleSheetHeaderResolver.normalize(value);
        if (normalized.contains("plan") || normalized.equals("oui")) return InterviewStatus.PLANNED;
        if (normalized.contains("real") || normalized.contains("termine")) return InterviewStatus.COMPLETED;
        if (normalized.contains("annul")) return InterviewStatus.CANCELLED;
        return InterviewStatus.NONE;
    }

    private ApplicationDecision decision(String value) {
        String normalized = GoogleSheetHeaderResolver.normalize(value);
        if (normalized.contains("accept")) return ApplicationDecision.ACCEPTED;
        if (normalized.contains("refus") || normalized.contains("reject")) {
            return ApplicationDecision.REJECTED;
        }
        if (normalized.contains("retir")) return ApplicationDecision.WITHDRAWN;
        return ApplicationDecision.PENDING;
    }

    private LocalDate date(String value, List<String> warnings) {
        if (blank(value) || yes(value) || Set.of("non", "no")
                .contains(GoogleSheetHeaderResolver.normalize(value))) return null;
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ofPattern("d/M/uuuu"),
                DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("d-M-uuuu"))) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Essayer le format suivant.
            }
        }
        warnings.add("Date non reconnue : « " + value + " ».");
        return null;
    }

    private String url(String value, String label, List<String> warnings) {
        if (blank(value)) return null;
        if (value.matches("(?i)^https?://.+")) return value.trim();
        warnings.add("Le " + label + " sans protocole a été ignoré : « " + value + " ».");
        return null;
    }

    private String optionalEmail(String value) {
        if (blank(value)) return null;
        String normalized = GoogleSheetHeaderResolver.normalize(value);
        if (Set.of("-", "n/a", "na", "non disponible", "non renseigne",
                "aucun", "inconnu").contains(normalized)) {
            return null;
        }
        return value.trim();
    }

    private boolean yes(String value) {
        return Set.of("oui", "yes", "x", "1")
                .contains(GoogleSheetHeaderResolver.normalize(value));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record Preview(GoogleSheetApplicationRow row, ApplicationForm form,
                          List<String> warnings) {
    }
}
