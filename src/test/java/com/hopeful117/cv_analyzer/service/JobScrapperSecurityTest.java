package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobScrapperSecurityTest {
    private final JobScrapperService service = new JobScrapperService();

    @Test
    void rejectsLocalAndUnsupportedUrls() {
        assertThatThrownBy(() -> service.validatePublicHttpUrl("http://localhost:8080/admin"))
                .isInstanceOf(InvalidJobOfferException.class);
        assertThatThrownBy(() -> service.validatePublicHttpUrl("http://127.0.0.1/internal"))
                .isInstanceOf(InvalidJobOfferException.class);
        assertThatThrownBy(() -> service.validatePublicHttpUrl("file:///etc/passwd"))
                .isInstanceOf(InvalidJobOfferException.class);
    }
}
