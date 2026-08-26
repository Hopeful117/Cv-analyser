package com.hopeful117.cv_analyzer.WebInterfaceController;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou UX (régressions réelles constatées) : la vue d'ensemble ne doit plus présenter
 * comme « Prochainement » une fonctionnalité déjà disponible et accessible depuis la navigation.
 */
class HomeOverviewTemplateTest {

    private static final String HOME = "src/main/resources/templates/home.html";

    @Test
    void professionalProfileIsPresentedAsAvailable() throws IOException {
        String home = Files.readString(Path.of(HOME));
        assertThat(home).contains("Profil professionnel · Disponible");
        assertThat(home).doesNotContain("Profil professionnel · Prochainement");
    }

    @Test
    void availableTreatmentMatchesTheConventionUsedByApplicationTracking() throws IOException {
        String home = Files.readString(Path.of(HOME));
        String successBadge = "<span class=\"os-badge os-badge-success\">";
        assertThat(home).contains(successBadge + "Suivi des candidatures · Disponible");
        assertThat(home).contains(successBadge + "Profil professionnel · Disponible");
    }
}
