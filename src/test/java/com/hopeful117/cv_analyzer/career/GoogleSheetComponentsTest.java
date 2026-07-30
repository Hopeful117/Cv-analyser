package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetFormulaEscaper;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetHeaderResolver;
import com.hopeful117.cv_analyzer.career.infrastructure.google.GoogleSheetsFunctionalException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GoogleSheetComponentsTest {
    private final GoogleSheetHeaderResolver resolver = new GoogleSheetHeaderResolver();

    @Test
    void resolvesAccentsCaseWhitespaceAndAliases() {
        var headers = resolver.resolve(List.of(" entreprise ", "POSTE", "Statut",
                "career_intelligence_id", "Lettre envoyée", "last_synchronized_at"), false);
        assertThat(headers.require("Entreprise")).isZero();
        assertThat(headers.require("Career Intelligence ID")).isEqualTo(3);
        assertThat(headers.find("LM envoyée")).hasValue(4);
        assertThat(headers.find("Dernière synchronisation")).hasValue(5);
        assertThat(headers.missingRequired()).isEmpty();
    }

    @Test
    void reportsMissingColumnsAndRejectsDuplicates() {
        assertThat(resolver.resolve(List.of("Entreprise", "Poste"), false).missingRequired())
                .containsExactly("Statut", "Career Intelligence ID");
        assertThatThrownBy(() -> resolver.resolve(List.of("Poste", " poste "), true))
                .isInstanceOf(GoogleSheetsFunctionalException.class)
                .hasMessageContaining("plusieurs fois");
    }

    @Test
    void formulaEscaperProtectsOnlyPotentialFormulas() {
        assertThat(GoogleSheetFormulaEscaper.escape("=IMPORTXML(...)")).isEqualTo("'=IMPORTXML(...)");
        assertThat(GoogleSheetFormulaEscaper.escape("+331234")).isEqualTo("'+331234");
        assertThat(GoogleSheetFormulaEscaper.escape("-test")).isEqualTo("'-test");
        assertThat(GoogleSheetFormulaEscaper.escape("@mention")).isEqualTo("'@mention");
        assertThat(GoogleSheetFormulaEscaper.escape("Texte sûr")).isEqualTo("Texte sûr");
    }
}
