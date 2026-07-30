package com.hopeful117.cv_analyzer.career.infrastructure.google;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleSheetHeaderResolver {
    public static final List<String> REQUIRED = List.of(
            "Entreprise", "Poste", "Statut", "Career Intelligence ID");
    private static final Map<String, String> ALIASES = Map.of(
            normalize("Lettre envoyée"), normalize("LM envoyée"),
            normalize("URL offre"), normalize("Lien offre"),
            normalize("career_intelligence_id"), normalize("Career Intelligence ID"),
            normalize("last_synchronized_at"), normalize("Dernière synchronisation"));

    public ResolvedHeaders resolve(List<?> rawHeaders, boolean allowLegacyMissingId) {
        Map<String, Integer> byCanonicalName = new LinkedHashMap<>();
        List<String> displayed = new ArrayList<>();
        for (int i = 0; i < rawHeaders.size(); i++) {
            String displayedName = Objects.toString(rawHeaders.get(i), "").trim();
            displayed.add(displayedName);
            String normalized = canonical(displayedName);
            if (!normalized.isBlank()) {
                if (byCanonicalName.putIfAbsent(normalized, i) != null) {
                    throw new GoogleSheetsFunctionalException("DUPLICATE_HEADER",
                            "L’en-tête « " + displayedName + " » est présent plusieurs fois.");
                }
            }
        }
        List<String> missing = REQUIRED.stream()
                .filter(name -> !(allowLegacyMissingId && name.equals("Career Intelligence ID")))
                .filter(name -> !byCanonicalName.containsKey(canonical(name))).toList();
        return new ResolvedHeaders(displayed, byCanonicalName, missing);
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[_\\s]+", " ");
    }

    private static String canonical(String value) {
        String normalized = normalize(value);
        return ALIASES.getOrDefault(normalized, normalized);
    }

    public record ResolvedHeaders(List<String> displayed, Map<String, Integer> indexes,
                                  List<String> missingRequired) {
        public int require(String name) {
            Integer index = indexes.get(canonical(name));
            if (index == null) throw new GoogleSheetsFunctionalException("MISSING_COLUMN",
                    "La colonne obligatoire « " + name + " » est absente.");
            return index;
        }
        public OptionalInt find(String name) {
            Integer index = indexes.get(canonical(name));
            return index == null ? OptionalInt.empty() : OptionalInt.of(index);
        }
        public Map<String, Integer> displayIndexes() {
            return indexes.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
