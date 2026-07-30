package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.infrastructure.google.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleSheetConsultationRowMapperTest {
    @Test
    void mapsReorderedColumnsAndAliasesWithoutNumericAssumptions() {
        List<String> headers = List.of("Statut", "career_intelligence_id", "Poste",
                "Entreprise", "Priorité", "Relance prévue", "Date candidature",
                "Statut synchronisation");
        var resolved = new GoogleSheetHeaderResolver().resolve(headers, false);
        var row = new GoogleSheetConsultationRowMapper().map(7,
                List.of("En attente", "APPLICATION-12", "Java Developer", "Acme",
                        "Haute", "20/08/2026", "10/08/2026", "SYNCHRONIZED"),
                resolved);

        assertThat(row.rowNumber()).isEqualTo(7);
        assertThat(row.companyName()).isEqualTo("Acme");
        assertThat(row.jobTitle()).isEqualTo("Java Developer");
        assertThat(row.careerIntelligenceId()).isEqualTo("APPLICATION-12");
        assertThat(row.valid()).isTrue();
        assertThat(row.columns()).containsEntry("Statut", "En attente");
    }

    @Test
    void keepsLegacyRowsWithoutStableIdentityConsultable() {
        var resolved = new GoogleSheetHeaderResolver().resolve(
                List.of("Entreprise", "Poste", "Statut", "Career Intelligence ID"), false);
        var row = new GoogleSheetConsultationRowMapper().map(2,
                List.of("Acme", "Java", "En attente", ""), resolved);
        assertThat(row.valid()).isTrue();
    }

    @Test
    void marksRowsWithoutCompanyOrStatusAsInvalid() {
        var resolved = new GoogleSheetHeaderResolver().resolve(
                List.of("Entreprise", "Poste", "Statut", "Career Intelligence ID"), false);
        var row = new GoogleSheetConsultationRowMapper().map(2,
                List.of("", "Java", "", ""), resolved);
        assertThat(row.valid()).isFalse();
    }
}
