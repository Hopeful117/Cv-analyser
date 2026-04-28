package com.hopeful117.cv_analyzer.extractor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class JobKeyWordExtractor {
    private static final Set<String> TECH_KEYWORDS = Set.of(
            "java","spring","aws","docker",
            "kubernetes","sql","api","git",
            "react","python","django"
    );

    public List<String> extract(String text){

        String lower = text.toLowerCase();

        List<String> keywords = new ArrayList<>();

        for(String keyword : TECH_KEYWORDS){

            if(lower.contains(keyword)){

                keywords.add(keyword);

            }
        }

        return keywords;
    }
}
