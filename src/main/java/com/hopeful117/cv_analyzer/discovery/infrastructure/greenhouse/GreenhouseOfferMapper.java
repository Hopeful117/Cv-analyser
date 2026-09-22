package com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse;

import com.hopeful117.cv_analyzer.discovery.domain.JobOffer;
import org.jsoup.Jsoup;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class GreenhouseOfferMapper {

    private GreenhouseOfferMapper() {
    }

    public static JobOffer toDomain(GreenhouseJobResponse.GreenhouseJobDto dto,
                                    GreenhouseProperties.Board board) {
        if (dto == null) return null;

        return new JobOffer(
                "greenhouse",
                dto.id() != null ? dto.id().toString() : null,
                dto.absoluteUrl(),
                Instant.now(),
                dto.title(),
                cleanDescription(dto.content()),
                board.companyName(),
                null,
                null,
                null,
                location(dto),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                parseInstant(dto.firstPublished()),
                parseInstant(dto.updatedAt())
        );
    }

    private static String cleanDescription(String content) {
        if (content == null || content.isBlank()) return null;
        String text = Jsoup.parse(content).text().trim();
        return text.isBlank() ? null : text;
    }

    private static String location(GreenhouseJobResponse.GreenhouseJobDto dto) {
        String direct = dto.location() != null ? dto.location().name() : null;
        if (direct != null && !direct.isBlank()) return direct;
        return safe(dto.offices()).stream()
                .flatMap(office -> Stream.of(office.name(), office.location()))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse(null);
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
