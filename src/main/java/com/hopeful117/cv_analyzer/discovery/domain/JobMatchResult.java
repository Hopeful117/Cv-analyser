package com.hopeful117.cv_analyzer.discovery.domain;

import java.util.Collections;
import java.util.List;

public record JobMatchResult(
        int score,
        List<MatchSignal> positiveSignals,
        List<MatchSignal> gaps,
        List<MatchSignal> unknowns
) {
    public JobMatchResult {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Le score de matching doit être compris entre 0 et 100.");
        }
        positiveSignals = immutable(positiveSignals);
        gaps = immutable(gaps);
        unknowns = immutable(unknowns);
    }

    public static JobMatchResult unknown(String message) {
        return new JobMatchResult(50, List.of(), List.of(),
                List.of(new MatchSignal(MatchSignalType.UNKNOWN, message)));
    }

    private static <T> List<T> immutable(List<T> values) {
        return values == null ? List.of() : Collections.unmodifiableList(List.copyOf(values));
    }
}
