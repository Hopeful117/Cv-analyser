package com.hopeful117.cv_analyzer.career.application.importer;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetHeaderResolver;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import com.hopeful117.cv_analyzer.exception.InvalidUploadException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExcelApplicationImportService {
    private static final int MAX_ROWS = 10_000;
    private final ApplicationCrmService crmService;
    private final Map<String, PendingImport> pending = new ConcurrentHashMap<>();

    public Preview preview(MultipartFile file) {
        validate(file);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = Optional.ofNullable(workbook.getSheet("Candidatures"))
                    .orElse(workbook.getSheetAt(0));
            DataFormatter formatter = new DataFormatter(Locale.FRANCE);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw invalid("Le fichier ne contient pas d’en-têtes.");
            List<String> rawHeaders = new ArrayList<>();
            for (int i = 0; i < header.getLastCellNum(); i++)
                rawHeaders.add(formatter.formatCellValue(header.getCell(i)).trim());
            var headers = new GoogleSheetHeaderResolver().resolve(rawHeaders, true);
            List<String> missing = List.of("Entreprise", "Poste", "Statut").stream()
                    .filter(name -> headers.find(name).isEmpty()).toList();
            if (!missing.isEmpty()) throw invalid("Colonnes obligatoires absentes : " +
                    String.join(", ", missing));

            List<ImportRow> rows = new ArrayList<>();
            int last = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + MAX_ROWS);
            for (int number = sheet.getFirstRowNum() + 1; number <= last; number++) {
                Row row = sheet.getRow(number);
                if (row != null && !empty(row, formatter))
                    rows.add(parse(row, number + 1, headers, formatter));
            }
            String token = UUID.randomUUID().toString();
            pending.put(token, new PendingImport(Instant.now(), rows));
            long valid = rows.stream().filter(ImportRow::valid).count();
            long duplicates = rows.stream().filter(ImportRow::duplicate).count();
            List<String> warnings = new ArrayList<>();
            if (sheet.getLastRowNum() > last) warnings.add("Import limité à " + MAX_ROWS + " lignes.");
            rows.forEach(row -> warnings.addAll(row.warnings()));
            return new Preview(token, rows.size(), valid, rows.size() - valid, valid - duplicates,
                    valid - duplicates, valid - duplicates, duplicates,
                    rows.stream().flatMap(row -> row.errors().stream()).toList(), warnings,
                    rows.stream().limit(100).toList());
        } catch (IOException exception) {
            throw invalid("Impossible de lire ce fichier Excel.");
        }
    }

    public ImportReport confirm(String token) {
        PendingImport value = pending.remove(token);
        if (value == null || value.createdAt().isBefore(Instant.now().minus(Duration.ofHours(1))))
            throw invalid("Cette prévisualisation a expiré.");
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        for (ImportRow row : value.rows()) {
            if (!row.valid() || row.duplicate()) { skipped++; continue; }
            try {
                crmService.createImported(row.form(), row.externalId());
                imported++;
            } catch (RuntimeException exception) {
                skipped++;
                errors.add("Ligne " + row.rowNumber() + " : import impossible.");
            }
        }
        return new ImportReport(imported, skipped, errors);
    }

    private ImportRow parse(Row row, int number, GoogleSheetHeaderResolver.ResolvedHeaders h,
                            DataFormatter formatter) {
        ApplicationForm form = new ApplicationForm();
        List<String> errors = new ArrayList<>(), warnings = new ArrayList<>();
        form.setCompanyName(text(row, h, "Entreprise", formatter));
        form.setCity(text(row, h, "Ville", formatter));
        form.setAddress(text(row, h, "Adresse", formatter));
        form.setPhone(text(row, h, "Téléphone", formatter));
        form.setEmail(optionalEmail(text(row, h, "Email", formatter)));
        form.setWebsite(url(text(row, h, "Site", formatter), warnings));
        form.setJobTitle(text(row, h, "Poste", formatter));
        form.setOfferUrl(url(text(row, h, "Lien offre", formatter), warnings));
        form.setContractTypeRaw(text(row, h, "Contrat", formatter));
        form.setWorkScheduleRaw(text(row, h, "Temps", formatter));
        form.setRemoteMode(remote(text(row, h, "Télétravail", formatter)));
        form.setSource(text(row, h, "Source", formatter));
        form.setPortfolioSent(yes(text(row, h, "Portfolio", formatter)));
        form.setAppliedAt(date(row, h, "Date candidature", formatter, warnings));
        form.setFollowUpPlannedAt(date(row, h, "Relance prévue", formatter, warnings));
        form.setLastFollowUpAt(date(row, h, "Dernière relance", formatter, warnings));
        form.setStatus(status(text(row, h, "Statut", formatter), warnings));
        form.setInterviewStatus(interview(text(row, h, "Entretien", formatter)));
        form.setDecision(decision(text(row, h, "Décision", formatter)));
        form.setSalaryText(text(row, h, "Salaire", formatter));
        form.setDistanceText(text(row, h, "Distance", formatter));
        form.setPriority(priority(text(row, h, "Priorité", formatter), warnings));
        form.setNotes(text(row, h, "Notes", formatter));
        String externalId = text(row, h, "Career Intelligence ID", formatter);
        if (blank(form.getCompanyName())) errors.add("Ligne " + number + " : entreprise absente.");
        if (blank(form.getJobTitle())) errors.add("Ligne " + number + " : poste absent.");
        boolean duplicate = errors.isEmpty() && crmService.isPotentialDuplicate(externalId,
                form.getCompanyName(), form.getJobTitle(), form.getAppliedAt());
        if (duplicate) warnings.add("Ligne " + number + " : doublon potentiel ignoré.");
        return new ImportRow(number, form, externalId, errors.isEmpty(), duplicate, errors, warnings);
    }

    private String text(Row row, GoogleSheetHeaderResolver.ResolvedHeaders h, String name,
                        DataFormatter formatter) {
        var index = h.find(name);
        if (index.isEmpty()) return null;
        Cell cell = row.getCell(index.getAsInt(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null || cell.getCellType() == CellType.FORMULA) return null;
        String value = formatter.formatCellValue(cell).trim();
        return value.isBlank() ? null : value;
    }
    private LocalDate date(Row row, GoogleSheetHeaderResolver.ResolvedHeaders h, String name,
                           DataFormatter formatter, List<String> warnings) {
        var index = h.find(name);
        if (index.isEmpty()) return null;
        Cell cell = row.getCell(index.getAsInt(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null || cell.getCellType() == CellType.FORMULA) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalDate();
            String value = formatter.formatCellValue(cell).trim();
            if (value.isBlank()) return null;
            for (DateTimeFormatter format : List.of(DateTimeFormatter.ofPattern("d/M/uuuu"),
                    DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("d-M-uuuu")))
                try { return LocalDate.parse(value, format); } catch (DateTimeParseException ignored) {}
        } catch (RuntimeException ignored) {}
        warnings.add("Date non reconnue dans « " + name + " ».");
        return null;
    }
    private ApplicationStatus status(String value, List<String> warnings) {
        if (blank(value)) return ApplicationStatus.NOT_CONTACTED;
        return switch (GoogleSheetHeaderResolver.normalize(value)) {
            case "non demarche" -> ApplicationStatus.NOT_CONTACTED;
            case "candidature envoyee", "postule" -> ApplicationStatus.APPLIED;
            case "en attente" -> ApplicationStatus.WAITING;
            case "entretien" -> ApplicationStatus.INTERVIEW;
            case "relance effectuee", "relance" -> ApplicationStatus.FOLLOWED_UP;
            case "refus", "refuse" -> ApplicationStatus.REJECTED;
            case "succes", "accepte" -> ApplicationStatus.SUCCESS;
            case "archive" -> ApplicationStatus.ARCHIVED;
            default -> { warnings.add("Statut inconnu « " + value + " » : Non démarché utilisé."); yield ApplicationStatus.NOT_CONTACTED; }
        };
    }
    private ApplicationPriority priority(String value, List<String> warnings) {
        if (blank(value)) return ApplicationPriority.MEDIUM;
        return switch (GoogleSheetHeaderResolver.normalize(value)) {
            case "faible", "low" -> ApplicationPriority.LOW;
            case "haute", "high" -> ApplicationPriority.HIGH;
            case "moyenne", "medium" -> ApplicationPriority.MEDIUM;
            default -> { warnings.add("Priorité inconnue « " + value + " » : Moyenne utilisée."); yield ApplicationPriority.MEDIUM; }
        };
    }
    private RemoteMode remote(String value) {
        String n = GoogleSheetHeaderResolver.normalize(value);
        if (n.contains("hybrid") || n.contains("hybride")) return RemoteMode.HYBRID;
        if (n.contains("remote") || n.contains("distance") || n.equals("oui")) return RemoteMode.REMOTE;
        if (n.contains("site") || n.contains("presentiel") || n.equals("non")) return RemoteMode.ONSITE;
        return RemoteMode.UNSPECIFIED;
    }
    private InterviewStatus interview(String value) {
        String n = GoogleSheetHeaderResolver.normalize(value);
        if (n.contains("plan") || n.equals("oui")) return InterviewStatus.PLANNED;
        if (n.contains("real") || n.contains("termine")) return InterviewStatus.COMPLETED;
        if (n.contains("annul")) return InterviewStatus.CANCELLED;
        return InterviewStatus.NONE;
    }
    private ApplicationDecision decision(String value) {
        String n = GoogleSheetHeaderResolver.normalize(value);
        if (n.contains("accept")) return ApplicationDecision.ACCEPTED;
        if (n.contains("refus") || n.contains("reject")) return ApplicationDecision.REJECTED;
        if (n.contains("retir")) return ApplicationDecision.WITHDRAWN;
        return ApplicationDecision.PENDING;
    }
    private String url(String value, List<String> warnings) {
        if (blank(value)) return null;
        if (value.matches("(?i)^https?://.+")) return value;
        warnings.add("URL sans protocole ignorée : " + value);
        return null;
    }
    private boolean yes(String value) {
        return Set.of("oui", "yes", "x", "1").contains(GoogleSheetHeaderResolver.normalize(value));
    }
    private String optionalEmail(String value) {
        if (blank(value)) return null;
        String normalized = GoogleSheetHeaderResolver.normalize(value);
        return Set.of("-", "n/a", "na", "non disponible", "non renseigne",
                "aucun", "inconnu").contains(normalized) ? null : value.trim();
    }
    private boolean empty(Row row, DataFormatter formatter) {
        for (Cell cell : row) if (!formatter.formatCellValue(cell).trim().isBlank()) return false;
        return true;
    }
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("Sélectionnez un fichier .xlsx.");
        if (file.getOriginalFilename() == null ||
                !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
            throw invalid("Seuls les fichiers .xlsx sont acceptés.");
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private InvalidUploadException invalid(String message) { return new InvalidUploadException(message); }

    private record PendingImport(Instant createdAt, List<ImportRow> rows) {}
    public record ImportRow(int rowNumber, ApplicationForm form, String externalId, boolean valid,
                            boolean duplicate, List<String> errors, List<String> warnings) {}
    public record Preview(String token, int detected, long valid, long ignored, long companies,
                          long opportunities, long applications, long duplicates,
                          List<String> errors, List<String> warnings, List<ImportRow> sample) {}
    public record ImportReport(int imported, int skipped, List<String> errors) {}
}
