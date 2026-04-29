package com.hopeful117.cv_analyzer.extractor;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class ImportanceSignals {
    public static final Set<String> MANDATORY_SIGNALS =
            Set.of(

                    // English
                    "required",
                    "must have",
                    "mandatory",
                    "essential",
                    "strong knowledge",

                    // Français
                    "obligatoire",
                    "indispensable",
                    "requis",
                    "maîtrise de",
                    "bonne connaissance de"
            );
    public static final Set<String> OPTIONAL_SIGNALS =
            Set.of(

                    // English
                    "preferred",
                    "bonus",
                    "nice to have",
                    "optional",

                    // Français
                    "serait un plus",
                    "apprécié",
                    "souhaité",
                    "optionnel"
            );
}
