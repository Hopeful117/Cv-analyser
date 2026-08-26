package com.hopeful117.cv_analyzer.profile.domain;

import java.text.Normalizer;

public final class ProfileNormalizer {

    private ProfileNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lowered = value.trim().toLowerCase(java.util.Locale.ROOT);
        String withoutAccents = Normalizer.normalize(lowered, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.replaceAll("[^a-z0-9+#. ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
