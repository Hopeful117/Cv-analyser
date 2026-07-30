package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.consultation.*;
import com.hopeful117.cv_analyzer.career.application.port.ApplicationSheetProjection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleSheetComparisonUseCaseTest {
    private final CompareGoogleSheetApplicationUseCase useCase =
            new CompareGoogleSheetApplicationUseCase();

    @Test
    void detectsSynchronizedAndDifferentRows() {
        ApplicationSheetProjection crm = projection();
        Map<String, String> exact = useCase.crmColumns(crm);
        GoogleSheetApplicationRow row = row(exact, true);
        assertThat(useCase.compare(row, crm, false).state())
                .isEqualTo(GoogleSheetComparisonState.SYNCHRONIZED);

        var changed = new java.util.LinkedHashMap<>(exact);
        changed.put("Statut", "Entretien");
        var result = useCase.compare(row(changed, true), crm, false);
        assertThat(result.state()).isEqualTo(GoogleSheetComparisonState.DIFFERENT);
        assertThat(result.differences()).singleElement()
                .extracting(GoogleSheetDifference::field).isEqualTo("Statut");
    }

    @Test
    void detectsMissingDuplicateAndInvalidStates() {
        ApplicationSheetProjection crm = projection();
        GoogleSheetApplicationRow valid = row(useCase.crmColumns(crm), true);
        assertThat(useCase.compare(valid, null, false).state())
                .isEqualTo(GoogleSheetComparisonState.MISSING_IN_CRM);
        assertThat(useCase.compare(valid, crm, true).state())
                .isEqualTo(GoogleSheetComparisonState.DUPLICATE_EXTERNAL_ID);
        assertThat(useCase.compare(row(Map.of(), false), crm, false).state())
                .isEqualTo(GoogleSheetComparisonState.INVALID);
        assertThat(useCase.missingInSheet(crm).state())
                .isEqualTo(GoogleSheetComparisonState.MISSING_IN_SHEET);
    }

    private GoogleSheetApplicationRow row(Map<String, String> columns, boolean valid) {
        return new GoogleSheetApplicationRow(2, columns, "Acme", "Java Developer",
                columns.getOrDefault("Statut", ""), columns.getOrDefault("Priorité", ""),
                columns.getOrDefault("Date candidature", ""),
                columns.getOrDefault("Relance prévue", ""), "APPLICATION-12",
                "SYNCHRONIZED", valid);
    }

    private ApplicationSheetProjection projection() {
        return new ApplicationSheetProjection("12", "APPLICATION-12", "Acme", "Paris",
                null, null, null, null, "Java Developer", null, "CDI",
                "FULL_TIME", "HYBRID", "LinkedIn", true, false, true,
                LocalDate.of(2026, 8, 10), null, null, "En attente", "NONE",
                "PENDING", null, null, "Haute", "Note", 82, 3, null, "SYNCHRONIZED");
    }
}
