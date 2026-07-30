package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionService;
import com.hopeful117.cv_analyzer.career.application.consultation.*;
import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsConsultationPort;
import com.hopeful117.cv_analyzer.career.domain.ApplicationStatus;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ImportGoogleSheetApplicationUseCaseTest {
    @Test
    void previewsLegacyRowAndUsesCvSentDateAsApplicationDate() {
        GoogleSheetsConsultationPort port = mock(GoogleSheetsConsultationPort.class);
        var service = new ImportGoogleSheetApplicationUseCase(port,
                mock(ApplicationCrmService.class), mock(ApplicationProjectionService.class));
        when(port.readApplications()).thenReturn(report(row()));

        var preview = service.preview(3);

        assertThat(preview.form().getCompanyName()).isEqualTo("Acme");
        assertThat(preview.form().getJobTitle()).isNull();
        assertThat(preview.form().getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(preview.form().getAppliedAt()).isEqualTo(LocalDate.of(2026, 7, 29));
        assertThat(preview.warnings()).anyMatch(message -> message.contains("poste"));
    }

    @Test
    void treatsUnavailableHistoricalEmailAsAbsent() {
        GoogleSheetsConsultationPort port = mock(GoogleSheetsConsultationPort.class);
        var service = new ImportGoogleSheetApplicationUseCase(port,
                mock(ApplicationCrmService.class), mock(ApplicationProjectionService.class));
        GoogleSheetApplicationRow source = row();
        Map<String, String> columns = new java.util.LinkedHashMap<>(source.columns());
        columns.put("Email", "Non disponible");
        GoogleSheetApplicationRow withUnavailableEmail = new GoogleSheetApplicationRow(
                source.rowNumber(), columns, source.companyName(), source.jobTitle(),
                source.status(), source.priority(), source.appliedAt(),
                source.followUpPlannedAt(), source.careerIntelligenceId(),
                source.synchronizationStatus(), source.valid());
        when(port.readApplications()).thenReturn(report(withUnavailableEmail));

        assertThat(service.preview(3).form().getEmail()).isNull();
    }

    @Test
    void confirmedImportCreatesMysqlRecordThenProjectsIt() {
        GoogleSheetsConsultationPort port = mock(GoogleSheetsConsultationPort.class);
        ApplicationCrmService crm = mock(ApplicationCrmService.class);
        ApplicationProjectionService projection = mock(ApplicationProjectionService.class);
        var service = new ImportGoogleSheetApplicationUseCase(port, crm, projection);
        when(port.readApplications()).thenReturn(report(row()));
        ApplicationForm form = new ApplicationForm();
        form.setCompanyName("Acme");
        form.setJobTitle("Développeur Java");
        form.setStatus(ApplicationStatus.APPLIED);
        when(crm.createImportedFromGoogleSheet(form, 3)).thenReturn(42L);

        long id = service.importRow(3, form);

        assertThat(id).isEqualTo(42L);
        var order = inOrder(crm, projection);
        order.verify(crm).createImportedFromGoogleSheet(form, 3);
        order.verify(projection).synchronize(42L);
    }

    private GoogleSheetConsultationReport report(GoogleSheetApplicationRow row) {
        return new GoogleSheetConsultationReport(new GoogleSheetApplicationSnapshot(
                row.columns().keySet().stream().toList(), List.of(row), Instant.now()),
                List.of(), List.of(), Instant.now());
    }

    private GoogleSheetApplicationRow row() {
        Map<String, String> columns = Map.of(
                "Entreprise", "Acme",
                "Poste", "",
                "Statut", "Candidature envoyée",
                "CV envoyé", "29/07/2026",
                "Career Intelligence ID", "");
        return new GoogleSheetApplicationRow(3, columns, "Acme", "",
                "Candidature envoyée", "Faible", "", "", "", "", true);
    }
}
