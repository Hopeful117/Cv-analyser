package com.hopeful117.cv_analyzer.extractor;

import com.hopeful117.cv_analyzer.model.SkillRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class JobKeyWordExtractor implements KeywordExtractor {
    private final ImportanceDetector importanceDetector;
    private static final Set<String> TECH_KEYWORDS = Set.of(
            "java","spring","aws","docker",
            "kubernetes","sql","api","git",
            "react","python","django"
    );

    @Override
    public List<SkillRequirement> extract(String text){

        String lower = text.toLowerCase();

        List<SkillRequirement> skills = new ArrayList<>();
        TECH_KEYWORDS.forEach(keyword -> {
            if(lower.contains(keyword)){
                skills.add(importanceDetector.detect(keyword, lower));
            }
        });

        return skills;
    }
}
