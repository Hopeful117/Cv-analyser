package com.hopeful117.cv_analyzer.career;

import com.hopeful117.cv_analyzer.career.application.port.GoogleSheetsProjectionPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "career.google-sheets.enabled=true",
        "career.google-sheets.spreadsheet-id=test-spreadsheet"
})
class GoogleSheetsStartupWithoutCredentialsTest {
    @Autowired GoogleSheetsProjectionPort projectionPort;

    @Test
    void applicationContextStartsWithoutResolvingCredentials() {
        assertThat(projectionPort).isNotNull();
    }
}
