package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsProjectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "career.google-sheets", name = "enabled", havingValue = "true")
public class GoogleSheetsProjectionAdapter implements GoogleSheetsProjectionPort {
    private static final Pattern LAST_ROW = Pattern.compile(".*![A-Z]+(\\d+):[A-Z]+(\\d+)");
    private final GoogleSheetsClient client;
    private final CareerGoogleSheetsProperties properties;
    private final GoogleSheetHeaderResolver headerResolver = new GoogleSheetHeaderResolver();
    private final GoogleSheetRowMapper rowMapper = new GoogleSheetRowMapper();

    @Override
    public ConnectionReport validateConnection() {
        if (!properties.configured()) {
            return new ConnectionReport(false, properties.applicationsSheet(), 0, List.of(),
                    "NOT_CONFIGURED", "L’identifiant du tableur n’est pas configuré.");
        }
        try {
            List<String> headers = readHeaders();
            var resolved = headerResolver.resolve(headers, false);
            return new ConnectionReport(resolved.missingRequired().isEmpty(), properties.applicationsSheet(),
                    headers.size(), resolved.missingRequired(),
                    resolved.missingRequired().isEmpty() ? null : "MISSING_COLUMN",
                    resolved.missingRequired().isEmpty() ? "Connexion et en-têtes valides."
                            : "Des colonnes obligatoires sont absentes.");
        } catch (GoogleSheetsFunctionalException exception) {
            return new ConnectionReport(false, properties.applicationsSheet(), 0, List.of(),
                    exception.getCode(), exception.getMessage());
        } catch (RuntimeException exception) {
            return new ConnectionReport(false, properties.applicationsSheet(), 0, List.of(),
                    "CREDENTIALS_UNAVAILABLE",
                    "Les identifiants Google sont absents ou invalides.");
        }
    }

    @Override
    public List<String> readHeaders() {
        try {
            List<List<Object>> values = client.get().spreadsheets().values()
                    .get(properties.spreadsheetId(), range(properties.headerRow() + ":" + properties.headerRow()))
                    .execute().getValues();
            if (values == null || values.isEmpty()) {
                throw new GoogleSheetsFunctionalException("MISSING_HEADER",
                        "La ligne d’en-tête de l’onglet est vide.");
            }
            return values.getFirst().stream().map(String::valueOf).toList();
        } catch (IOException exception) {
            throw translate(exception);
        }
    }

    @Override
    public Optional<RemoteProjection> findByExternalId(String externalId) {
        SheetData data = readSheet();
        int idColumn = data.headers().require("Career Intelligence ID");
        List<Integer> matches = new ArrayList<>();
        for (int i = 1; i < data.rows().size(); i++) {
            if (externalId.equals(cell(data.rows().get(i), idColumn))) matches.add(i + properties.headerRow());
        }
        if (matches.size() > 1) throw new GoogleSheetsFunctionalException("DUPLICATE_EXTERNAL_ID",
                "Plusieurs lignes utilisent l’identifiant Career Intelligence " + externalId + ".");
        if (matches.isEmpty()) return Optional.empty();
        int rowNumber = matches.getFirst();
        List<Object> row = data.rows().get(rowNumber - properties.headerRow());
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < data.headers().displayed().size(); i++) {
            values.put(data.headers().displayed().get(i), cell(row, i));
        }
        return Optional.of(new RemoteProjection(rowNumber, values));
    }

    @Override
    public UpsertResult upsert(ApplicationSheetProjection projection) {
        SheetData data = readSheet();
        ensureRequired(data.headers());
        int idColumn = data.headers().require("Career Intelligence ID");
        List<Integer> matches = new ArrayList<>();
        for (int i = 1; i < data.rows().size(); i++) {
            if (projection.careerIntelligenceId().equals(cell(data.rows().get(i), idColumn))) {
                matches.add(i + properties.headerRow());
            }
        }
        if (matches.size() > 1) throw new GoogleSheetsFunctionalException("DUPLICATE_EXTERNAL_ID",
                "Deux lignes ou plus possèdent le même Career Intelligence ID.");
        return matches.isEmpty() ? append(projection, data.headers()) :
                update(projection, data.headers(), matches.getFirst());
    }

    @Override
    public RebuildReport rebuild(List<ApplicationSheetProjection> projections) {
        SheetData data = readSheet();
        ensureRequired(data.headers());
        int idColumn = data.headers().require("Career Intelligence ID");
        Map<String, Integer> rowsById = new HashMap<>();
        Set<String> duplicateIds = new HashSet<>();
        for (int i = 1; i < data.rows().size(); i++) {
            String id = cell(data.rows().get(i), idColumn);
            if (!id.isBlank() && rowsById.putIfAbsent(id, i + properties.headerRow()) != null) {
                duplicateIds.add(id);
            }
        }
        int updated = 0, appended = 0;
        List<String> errors = new ArrayList<>();
        for (ApplicationSheetProjection projection : projections) {
            try {
                if (duplicateIds.contains(projection.careerIntelligenceId())) {
                    throw new GoogleSheetsFunctionalException("DUPLICATE_EXTERNAL_ID",
                            "Identifiant présent sur plusieurs lignes.");
                }
                Integer row = rowsById.get(projection.careerIntelligenceId());
                UpsertResult result = row == null ? append(projection, data.headers())
                        : update(projection, data.headers(), row);
                if (result.created()) appended++; else updated++;
            } catch (RuntimeException exception) {
                errors.add(projection.careerIntelligenceId() + " : " + exception.getMessage());
            }
        }
        return new RebuildReport(projections.size(), updated, appended, errors.size(), errors);
    }

    private UpsertResult append(ApplicationSheetProjection projection,
                                GoogleSheetHeaderResolver.ResolvedHeaders headers) {
        List<Object> row = buildRow(projection, headers, -1);
        try {
            AppendValuesResponse response = client.get().spreadsheets().values()
                    .append(properties.spreadsheetId(), range("A:ZZ"), new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED").setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(false).execute();
            int rowNumber = parseRow(response.getUpdates() == null ? null : response.getUpdates().getUpdatedRange());
            writeDaysFormula(headers, rowNumber);
            return new UpsertResult(projection.careerIntelligenceId(), rowNumber, true);
        } catch (IOException exception) {
            throw translate(exception);
        }
    }

    private UpsertResult update(ApplicationSheetProjection projection,
                                GoogleSheetHeaderResolver.ResolvedHeaders headers, int rowNumber) {
        List<Object> row = buildRow(projection, headers, rowNumber);
        try {
            client.get().spreadsheets().values()
                    .update(properties.spreadsheetId(), range("A" + rowNumber + ":" +
                            columnName(headers.displayed().size()) + rowNumber),
                            new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED").execute();
            return new UpsertResult(projection.careerIntelligenceId(), rowNumber, false);
        } catch (IOException exception) {
            throw translate(exception);
        }
    }

    private List<Object> buildRow(ApplicationSheetProjection projection,
                                  GoogleSheetHeaderResolver.ResolvedHeaders headers, int rowNumber) {
        Map<String, Object> mapped = rowMapper.map(projection);
        List<Object> row = new ArrayList<>(Collections.nCopies(headers.displayed().size(), ""));
        mapped.forEach((header, value) -> headers.find(header).ifPresent(index -> row.set(index, value)));
        headers.find("Jours").ifPresent(index -> {
            if (rowNumber > 0 && headers.find("Date candidature").isPresent())
                row.set(index, daysFormula(headers, rowNumber));
        });
        return row;
    }

    private void writeDaysFormula(GoogleSheetHeaderResolver.ResolvedHeaders headers, int rowNumber)
            throws IOException {
        if (rowNumber < 1 || headers.find("Jours").isEmpty() ||
                headers.find("Date candidature").isEmpty()) return;
        int daysColumn = headers.find("Jours").getAsInt();
        String cell = columnName(daysColumn + 1) + rowNumber;
        client.get().spreadsheets().values().update(properties.spreadsheetId(), range(cell),
                new ValueRange().setValues(List.of(List.of(daysFormula(headers, rowNumber)))))
                .setValueInputOption("USER_ENTERED").execute();
    }

    private String daysFormula(GoogleSheetHeaderResolver.ResolvedHeaders headers, int rowNumber) {
        int dateColumn = headers.require("Date candidature");
        String dateCell = columnName(dateColumn + 1) + rowNumber;
        return "=IF(" + dateCell + "=\"\",\"\",TODAY()-" + dateCell + ")";
    }

    private SheetData readSheet() {
        try {
            List<List<Object>> rows = client.get().spreadsheets().values()
                    .get(properties.spreadsheetId(), range(properties.headerRow() + ":100000"))
                    .execute().getValues();
            if (rows == null || rows.isEmpty()) throw new GoogleSheetsFunctionalException(
                    "MISSING_HEADER", "La ligne d’en-tête est absente.");
            var headers = headerResolver.resolve(rows.getFirst(), false);
            return new SheetData(headers, rows);
        } catch (IOException exception) {
            throw translate(exception);
        }
    }

    private void ensureRequired(GoogleSheetHeaderResolver.ResolvedHeaders headers) {
        if (!headers.missingRequired().isEmpty()) throw new GoogleSheetsFunctionalException(
                "MISSING_COLUMN", "Colonnes obligatoires absentes : " +
                String.join(", ", headers.missingRequired()));
    }

    private GoogleSheetsFunctionalException translate(IOException exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        String code = message.contains("403") ? "ACCESS_DENIED" : message.contains("404")
                ? "SPREADSHEET_OR_SHEET_NOT_FOUND" : message.contains("429") ? "QUOTA_EXCEEDED" : "GOOGLE_IO_ERROR";
        return new GoogleSheetsFunctionalException(code,
                "La projection Google Sheets est momentanément indisponible.", exception);
    }

    private String range(String suffix) {
        return "'" + properties.applicationsSheet().replace("'", "''") + "'!" + suffix;
    }
    private static String cell(List<Object> row, int index) {
        return index < row.size() && row.get(index) != null ? String.valueOf(row.get(index)).trim() : "";
    }
    private static int parseRow(String updatedRange) {
        Matcher matcher = LAST_ROW.matcher(updatedRange == null ? "" : updatedRange);
        if (!matcher.matches()) throw new GoogleSheetsFunctionalException("INVALID_RESPONSE",
                "Google Sheets n’a pas retourné la ligne ajoutée.");
        return Integer.parseInt(matcher.group(1));
    }
    private static String columnName(int oneBased) {
        StringBuilder result = new StringBuilder();
        int value = oneBased;
        while (value > 0) {
            value--;
            result.insert(0, (char) ('A' + value % 26));
            value /= 26;
        }
        return result.toString();
    }
    private record SheetData(GoogleSheetHeaderResolver.ResolvedHeaders headers, List<List<Object>> rows) {}
}
