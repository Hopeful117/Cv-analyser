package com.hopeful117.cv_analyzer.career.application.consultation;

import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionQueryService;
import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsConsultationPort;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultGoogleSheetApplicationsUseCase {
    private final GoogleSheetsConsultationPort consultationPort;
    private final ApplicationProjectionQueryService crmQueryService;
    private final CompareGoogleSheetApplicationUseCase comparisonUseCase;

    public GoogleSheetConsultationView consult(String query, String sheetStatus,
                                               GoogleSheetComparisonState state,
                                               String sort, String direction, int page, int size) {
        GoogleSheetConsultationReport report = consultationPort.readApplications();
        List<ApplicationSheetProjection> crmApplications = crmQueryService.getAll();
        List<GoogleSheetComparisonResult> all = compare(report, crmApplications);

        Comparator<GoogleSheetComparisonResult> comparator = comparator(sort);
        if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
        List<GoogleSheetComparisonResult> filtered = all.stream()
                .filter(item -> state == null || item.state() == state)
                .filter(item -> matches(item, query))
                .filter(item -> matchesStatus(item, sheetStatus))
                .sorted(comparator).toList();
        int safeSize = Math.clamp(size, 1, 100);
        int safePage = Math.max(0, page);
        int from = Math.min(safePage * safeSize, filtered.size());
        int to = Math.min(from + safeSize, filtered.size());
        Page<GoogleSheetComparisonResult> resultPage = new PageImpl<>(
                filtered.subList(from, to), PageRequest.of(safePage, safeSize), filtered.size());
        return new GoogleSheetConsultationView(resultPage, report.snapshot().rows().size(),
                count(all, GoogleSheetComparisonState.SYNCHRONIZED),
                count(all, GoogleSheetComparisonState.DIFFERENT),
                count(all, GoogleSheetComparisonState.MISSING_IN_CRM),
                count(all, GoogleSheetComparisonState.MISSING_IN_SHEET),
                count(all, GoogleSheetComparisonState.DUPLICATE_EXTERNAL_ID),
                count(all, GoogleSheetComparisonState.INVALID), report.snapshot().readAt());
    }

    public GoogleSheetApplicationDetailView details(String externalId) {
        GoogleSheetConsultationReport report = consultationPort.readApplications();
        List<GoogleSheetApplicationRow> rows = report.snapshot().rows().stream()
                .filter(row -> Objects.equals(externalId, row.careerIntelligenceId())).toList();
        ApplicationSheetProjection crm = crmQueryService.getAll().stream()
                .filter(item -> Objects.equals(externalId, item.careerIntelligenceId()))
                .findFirst().orElse(null);
        if (rows.isEmpty() && crm == null) {
            throw new EntityNotFoundException("Cette projection Google Sheets est introuvable.");
        }
        GoogleSheetComparisonResult comparison = rows.isEmpty()
                ? comparisonUseCase.missingInSheet(crm)
                : comparisonUseCase.compare(rows.getFirst(), crm, rows.size() > 1);
        return new GoogleSheetApplicationDetailView(externalId, comparison, rows,
                crm == null ? Map.of() : comparisonUseCase.crmColumns(crm));
    }

    public GoogleSheetApplicationDetailView detailsByRowNumber(int rowNumber) {
        GoogleSheetConsultationReport report = consultationPort.readApplications();
        GoogleSheetApplicationRow row = report.snapshot().rows().stream()
                .filter(candidate -> candidate.rowNumber() == rowNumber)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cette ligne Google Sheets est introuvable."));
        ApplicationSheetProjection crm = row.careerIntelligenceId() == null
                || row.careerIntelligenceId().isBlank() ? null
                : crmQueryService.getAll().stream()
                .filter(item -> Objects.equals(row.careerIntelligenceId(),
                        item.careerIntelligenceId()))
                .findFirst().orElse(null);
        GoogleSheetComparisonResult comparison = comparisonUseCase.compare(row, crm, false);
        String displayId = row.careerIntelligenceId() == null
                || row.careerIntelligenceId().isBlank()
                ? "Ligne " + row.rowNumber() : row.careerIntelligenceId();
        return new GoogleSheetApplicationDetailView(displayId, comparison, List.of(row),
                crm == null ? Map.of() : comparisonUseCase.crmColumns(crm));
    }

    List<GoogleSheetComparisonResult> compare(GoogleSheetConsultationReport report,
                                              List<ApplicationSheetProjection> crmApplications) {
        Map<String, ApplicationSheetProjection> crmById = crmApplications.stream()
                .collect(Collectors.toMap(ApplicationSheetProjection::careerIntelligenceId,
                        Function.identity(), (first, ignored) -> first, LinkedHashMap::new));
        Map<String, Long> occurrences = report.snapshot().rows().stream()
                .filter(row -> row.careerIntelligenceId() != null &&
                        !row.careerIntelligenceId().isBlank())
                .collect(Collectors.groupingBy(GoogleSheetApplicationRow::careerIntelligenceId,
                        LinkedHashMap::new, Collectors.counting()));
        Set<String> seen = new HashSet<>();
        List<GoogleSheetComparisonResult> results = new ArrayList<>();
        for (GoogleSheetApplicationRow row : report.snapshot().rows()) {
            String id = row.careerIntelligenceId();
            ApplicationSheetProjection crm = crmById.get(id);
            results.add(comparisonUseCase.compare(row, crm,
                    id != null && !id.isBlank() && occurrences.getOrDefault(id, 0L) > 1));
            if (id != null && !id.isBlank()) seen.add(id);
        }
        crmApplications.stream()
                .filter(crm -> !seen.contains(crm.careerIntelligenceId()))
                .map(comparisonUseCase::missingInSheet).forEach(results::add);
        return List.copyOf(results);
    }

    private boolean matches(GoogleSheetComparisonResult item, String query) {
        if (query == null || query.isBlank()) return true;
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return StreamValues.of(item.companyName(), item.jobTitle(), item.careerIntelligenceId())
                .anyMatch(value -> value != null && value.toLowerCase(Locale.ROOT).contains(needle));
    }

    private boolean matchesStatus(GoogleSheetComparisonResult item, String sheetStatus) {
        if (sheetStatus == null || sheetStatus.isBlank()) return true;
        String actual = item.sheetRow() == null ? "" : item.sheetRow().status();
        return normalize(actual).equals(normalize(sheetStatus));
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(
                        Objects.toString(value, "").trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private Comparator<GoogleSheetComparisonResult> comparator(String sort) {
        Function<GoogleSheetComparisonResult, String> extractor = switch (
                sort == null ? "" : sort.toLowerCase(Locale.ROOT)) {
            case "company" -> GoogleSheetComparisonResult::companyName;
            case "status" -> item -> item.sheetRow() == null ? "" : item.sheetRow().status();
            case "state" -> item -> item.state().name();
            default -> GoogleSheetComparisonResult::jobTitle;
        };
        return Comparator.comparing(extractor,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    private long count(List<GoogleSheetComparisonResult> results,
                       GoogleSheetComparisonState state) {
        return results.stream().filter(item -> item.state() == state).count();
    }

    private static final class StreamValues {
        static java.util.stream.Stream<String> of(String... values) {
            return Arrays.stream(values);
        }
    }
}
