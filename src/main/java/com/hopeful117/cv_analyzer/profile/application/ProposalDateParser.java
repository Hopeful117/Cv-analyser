package com.hopeful117.cv_analyzer.profile.application;

import com.hopeful117.cv_analyzer.exception.InvalidProfileException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parsing défensif des dates renvoyées par l'extraction IA : une date inexploitable est ignorée
 * (retour null) plutôt que rejetée, la proposition restant éditable par l'utilisateur.
 */
final class ProposalDateParser {

    private static final DateTimeFormatter ISO_STRICT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private ProposalDateParser() {
    }

    static LocalDate parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            return LocalDate.parse(value, ISO_STRICT);
        } catch (DateTimeParseException ignored) {
            // tentative mois seul ci-dessous
        }
        try {
            return YearMonth.parse(value, DateTimeFormatter.ofPattern("uuuu-MM")
                    .withResolverStyle(ResolverStyle.STRICT)).atDay(1);
        } catch (DateTimeParseException ignored) {
            // tentative année seule ci-dessous
        }
        try {
            int year = Integer.parseInt(value);
            if (year >= 1900 && year <= 2200) {
                return LocalDate.of(year, 1, 1);
            }
            return null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    static void validateRange(LocalDate startDate, LocalDate endDate, String itemLabel) {
        if (endDate != null && startDate == null) {
            throw new InvalidProfileException(itemLabel
                    + " : une date de fin est renseignée sans date de début.");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidProfileException(itemLabel
                    + " : la date de fin précède la date de début.");
        }
    }
}
