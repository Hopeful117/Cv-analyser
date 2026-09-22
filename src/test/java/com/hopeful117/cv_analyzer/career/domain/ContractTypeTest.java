package com.hopeful117.cv_analyzer.career.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTypeTest {

    @ParameterizedTest
    @CsvSource({
            "CDI, CDI",
            "permanent, CDI",
            "Permanent employment, CDI",
            "CDD, CDD",
            "fixed-term, CDD",
            "internship, STAGE",
            "temporary, INTERIM",
            "apprenticeship, ALTERNANCE"
    })
    void mapsDeterministicAliases(String raw, ContractType expected) {
        assertThat(ContractType.fromCode(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  ", "contract", "contractor", "unknown"})
    void preservesUnknownOrAmbiguousValuesAsUnknown(String raw) {
        assertThat(ContractType.fromCode(raw)).isNull();
    }
}
