package com.hopeful117.cv_analyzer.profile;

import com.hopeful117.cv_analyzer.exception.InvalidProfileException;
import com.hopeful117.cv_analyzer.profile.application.ProposalDateParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProposalDateParserTest {

    @Test
    void parsesIsoDate() {
        assertThat(ProposalDateParser.parse("2021-07-14")).isEqualTo(LocalDate.of(2021, 7, 14));
    }

    @Test
    void parsesYearMonthAsFirstDay() {
        assertThat(ProposalDateParser.parse("2021-07")).isEqualTo(LocalDate.of(2021, 7, 1));
    }

    @Test
    void parsesBareYear() {
        assertThat(ProposalDateParser.parse("2019")).isEqualTo(LocalDate.of(2019, 1, 1));
    }

    @Test
    void returnsNullForBlankNullGarbageOrOutOfRangeYears() {
        assertThat(ProposalDateParser.parse(null)).isNull();
        assertThat(ProposalDateParser.parse("   ")).isNull();
        assertThat(ProposalDateParser.parse("bientôt")).isNull();
        assertThat(ProposalDateParser.parse("1899")).isNull();
        assertThat(ProposalDateParser.parse("2201")).isNull();
        assertThat(ProposalDateParser.parse("2021-13-01")).isNull();
    }

    @Test
    void rejectsEndWithoutStart() {
        LocalDate end = LocalDate.of(2024, 1, 1);
        assertThatThrownBy(() -> ProposalDateParser.validateRange(null, end, "L’expérience"))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("sans date de début");
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> ProposalDateParser.validateRange(
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 1), "L’expérience"))
                .isInstanceOf(InvalidProfileException.class)
                .hasMessageContaining("précède");
    }

    @Test
    void acceptsOpenEndedExperience() {
        ProposalDateParser.validateRange(LocalDate.of(2023, 1, 1), null, "L’expérience");
    }
}
