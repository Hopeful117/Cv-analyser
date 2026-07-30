package com.hopeful117.cv_analyzer.career.infrastructure.google;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class GoogleSheetsClientTest {
    @AfterEach
    void clearProperty() {
        System.clearProperty("GOOGLE_APPLICATION_CREDENTIALS");
    }

    @Test
    void resolvesCredentialPathLoadedByDotenvAsSystemProperty() {
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "  ./google_secret/account.json  ");

        assertThat(GoogleSheetsClient.credentialPathFromSystemProperties())
                .isEqualTo(Path.of("./google_secret/account.json").toAbsolutePath().normalize());
    }

    @Test
    void returnsNullWhenNoLocalFallbackIsConfigured() {
        assertThat(GoogleSheetsClient.credentialPathFromSystemProperties()).isNull();
    }
}
