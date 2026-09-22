package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreenhouseOfferMapperTest {

    @Test
    void mapsBoardContextAndSanitizesHtmlDescription() {
        GreenhouseProperties.Board board = new GreenhouseProperties.Board("example", "Example Corp", true);
        GreenhouseJobResponse.GreenhouseJobDto dto = new GreenhouseJobResponse.GreenhouseJobDto(
                127817L,
                "Backend Engineer",
                "<p>Build APIs</p><script>alert('x')</script><p>with Java</p>",
                new GreenhouseJobResponse.Location("Paris"),
                List.of(new GreenhouseJobResponse.Office("Paris office", "France")),
                List.of(new GreenhouseJobResponse.Department("Engineering")),
                "2026-09-22T10:55:28-05:00",
                "2026-09-20T10:00:00Z",
                "https://boards.greenhouse.io/example/jobs/127817",
                "REQ-50");

        JobOffer offer = GreenhouseOfferMapper.toDomain(dto, board);

        assertThat(offer.providerKey()).isEqualTo("greenhouse");
        assertThat(offer.providerOfferId()).isEqualTo("127817");
        assertThat(offer.company()).isEqualTo("Example Corp");
        assertThat(offer.originUrl()).isEqualTo("https://boards.greenhouse.io/example/jobs/127817");
        assertThat(offer.title()).isEqualTo("Backend Engineer");
        assertThat(offer.description()).contains("Build APIs", "with Java").doesNotContain("script");
        assertThat(offer.locationLabel()).isEqualTo("Paris");
        assertThat(offer.romeLabel()).isNull();
        assertThat(offer.rawContractCode()).isNull();
        assertThat(offer.canonicalContractType()).isNull();
        assertThat(offer.salaryMinAmount()).isNull();
        assertThat(offer.providerCreatedAt()).isNotNull();
        assertThat(offer.providerUpdatedAt()).isNotNull();
    }

    @Test
    void missingOptionalFieldsRemainUnknown() {
        GreenhouseProperties.Board board = new GreenhouseProperties.Board("example", "Example Corp", true);
        GreenhouseJobResponse.GreenhouseJobDto dto = new GreenhouseJobResponse.GreenhouseJobDto(
                1L, "Role", null, null, null, null, null, null, null, null);

        JobOffer offer = GreenhouseOfferMapper.toDomain(dto, board);

        assertThat(offer.description()).isNull();
        assertThat(offer.locationLabel()).isNull();
        assertThat(offer.workMode()).isNull();
        assertThat(offer.competencies()).isEmpty();
        assertThat(offer.providerCreatedAt()).isNull();
    }
}
