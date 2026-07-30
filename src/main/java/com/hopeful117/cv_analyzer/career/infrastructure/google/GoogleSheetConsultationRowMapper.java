package com.hopeful117.cv_analyzer.career.infrastructure.google;

import com.hopeful117.cv_analyzer.career.application.consultation.GoogleSheetApplicationRow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class GoogleSheetConsultationRowMapper {
    public GoogleSheetApplicationRow map(int rowNumber, List<Object> rawRow,
                                         GoogleSheetHeaderResolver.ResolvedHeaders headers) {
        Map<String, String> columns = new LinkedHashMap<>();
        for (int index = 0; index < headers.displayed().size(); index++) {
            columns.put(headers.displayed().get(index), cell(rawRow, index));
        }
        String company = value(rawRow, headers, "Entreprise");
        String title = value(rawRow, headers, "Poste");
        String status = value(rawRow, headers, "Statut");
        String externalId = value(rawRow, headers, "Career Intelligence ID");
        // Les lignes historiques peuvent précéder Career Intelligence et ne possèdent donc
        // ni identifiant externe ni, parfois, intitulé de poste. Elles restent consultables :
        // seule l'absence des deux informations minimales les rend ininterprétables.
        boolean valid = !company.isBlank() && !status.isBlank();
        return new GoogleSheetApplicationRow(rowNumber,
                Collections.unmodifiableMap(new LinkedHashMap<>(columns)), company, title,
                status, value(rawRow, headers, "Priorité"),
                value(rawRow, headers, "Date candidature"),
                value(rawRow, headers, "Relance prévue"), externalId,
                value(rawRow, headers, "Statut synchronisation"), valid);
    }

    private String value(List<Object> row, GoogleSheetHeaderResolver.ResolvedHeaders headers,
                         String header) {
        var index = headers.find(header);
        return index.isEmpty() ? "" : cell(row, index.getAsInt());
    }

    private String cell(List<Object> row, int index) {
        return index < row.size() && row.get(index) != null
                ? String.valueOf(row.get(index)).trim() : "";
    }
}
