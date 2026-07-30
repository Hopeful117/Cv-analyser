package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionQueryService;
import com.hopeful117.cv_analyzer.career.application.consultation.*;
import com.hopeful117.cv_analyzer.career.application.port.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConsultGoogleSheetApplicationsUseCaseTest {
    @Test
    void calculatesDuplicateMissingAndInvalidDiagnosticsWithoutWriting() {
        GoogleSheetsConsultationPort port = mock(GoogleSheetsConsultationPort.class);
        ApplicationProjectionQueryService crm = mock(ApplicationProjectionQueryService.class);
        var comparison = new CompareGoogleSheetApplicationUseCase();
        var useCase = new ConsultGoogleSheetApplicationsUseCase(port, crm, comparison);
        var valid = row(2, "APPLICATION-1", true);
        var duplicate = row(3, "APPLICATION-1", true);
        var orphan = row(4, "APPLICATION-404", true);
        var invalid = row(5, "", false);
        when(port.readApplications()).thenReturn(new GoogleSheetConsultationReport(
                new GoogleSheetApplicationSnapshot(List.of(), List.of(valid, duplicate, orphan, invalid),
                        Instant.now()), List.of(), List.of(), Instant.now()));
        when(crm.getAll()).thenReturn(List.of(projection("1"), projection("2")));

        var view = useCase.consult(null, null, null, "title", "asc", 0, 25);

        assertThat(view.duplicateCount()).isEqualTo(2);
        assertThat(view.missingInCrmCount()).isEqualTo(1);
        assertThat(view.missingInSheetCount()).isEqualTo(1);
        assertThat(view.invalidCount()).isEqualTo(1);
        verify(port).readApplications();
        verifyNoMoreInteractions(port);
    }

    @Test
    void filtersHistoricalSentApplicationsWithoutWriting() {
        GoogleSheetsConsultationPort port = mock(GoogleSheetsConsultationPort.class);
        ApplicationProjectionQueryService crm = mock(ApplicationProjectionQueryService.class);
        var useCase = new ConsultGoogleSheetApplicationsUseCase(port, crm,
                new CompareGoogleSheetApplicationUseCase());
        var sent = new GoogleSheetApplicationRow(2, Map.of(
                "Entreprise", "Acme", "Poste", "", "Statut", "Candidature envoyée",
                "Career Intelligence ID", ""), "Acme", "", "Candidature envoyée",
                "Haute", "", "", "", "", true);
        var notContacted = new GoogleSheetApplicationRow(3, Map.of(
                "Entreprise", "Beta", "Poste", "", "Statut", "Non démarché",
                "Career Intelligence ID", ""), "Beta", "", "Non démarché",
                "Faible", "", "", "", "", true);
        when(port.readApplications()).thenReturn(new GoogleSheetConsultationReport(
                new GoogleSheetApplicationSnapshot(List.of(), List.of(sent, notContacted),
                        Instant.now()), List.of(), List.of(), Instant.now()));
        when(crm.getAll()).thenReturn(List.of());

        var view = useCase.consult(null, "Candidature envoyée", null,
                "company", "asc", 0, 25);

        assertThat(view.results().getTotalElements()).isEqualTo(1);
        assertThat(view.results().getContent().getFirst().companyName()).isEqualTo("Acme");
        assertThat(view.results().getContent().getFirst().state())
                .isEqualTo(GoogleSheetComparisonState.MISSING_IN_CRM);
        verify(port).readApplications();
        verifyNoMoreInteractions(port);
    }

    private GoogleSheetApplicationRow row(int number, String id, boolean valid) {
        return new GoogleSheetApplicationRow(number, Map.of(
                "Entreprise", "Acme", "Poste", "Java", "Statut", "En attente",
                "Career Intelligence ID", id), "Acme", "Java", "En attente",
                "Moyenne", "", "", id, "SYNCHRONIZED", valid);
    }

    private ApplicationSheetProjection projection(String id) {
        return new ApplicationSheetProjection(id, "APPLICATION-" + id, "Acme", null,
                null, null, null, null, "Java", null, null, null, null,
                null, false, false, false, null, null, null, "En attente",
                "NONE", "PENDING", null, null, "Moyenne", null, null, null,
                null, "SYNCHRONIZED");
    }
}
