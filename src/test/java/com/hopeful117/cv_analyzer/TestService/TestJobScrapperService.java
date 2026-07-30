package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import com.hopeful117.cv_analyzer.service.JobScrapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestJobScrapperService {
    private JobScrapperService jobScrapperService;

    @BeforeEach
    void setUp() {
        jobScrapperService = new JobScrapperService();
    }

    @Test
    void acceptsPublicHttpUrlWithoutPerformingNetworkRequest() {
        assertThatCode(() -> jobScrapperService.validatePublicHttpUrl("https://8.8.8.8/jobs/42"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidLocalPrivateAndUnsupportedUrls() {
        assertThatThrownBy(() -> jobScrapperService.validatePublicHttpUrl("not-an-url"))
                .isInstanceOf(InvalidJobOfferException.class);
        assertThatThrownBy(() -> jobScrapperService.validatePublicHttpUrl("http://localhost:8080"))
                .isInstanceOf(InvalidJobOfferException.class);
        assertThatThrownBy(() -> jobScrapperService.validatePublicHttpUrl("http://10.0.0.1/job"))
                .isInstanceOf(InvalidJobOfferException.class);
        assertThatThrownBy(() -> jobScrapperService.validatePublicHttpUrl("file:///tmp/job.html"))
                .isInstanceOf(InvalidJobOfferException.class);
    }
}
