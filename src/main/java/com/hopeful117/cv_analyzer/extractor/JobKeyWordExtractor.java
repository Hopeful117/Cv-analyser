package com.hopeful117.cv_analyzer.extractor;

import com.hopeful117.cv_analyzer.model.SkillRequirement;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class JobKeyWordExtractor implements KeywordExtractor {
    private static final Set<String> TECH_KEYWORDS = Set.of(
            "java","spring","aws","docker",
            "kubernetes","sql","api","git",
            "react","python","django"
    );

    @Override
    public List<SkillRequirement> extract(String text){

        String lower = text.toLowerCase();

        List<SkillRequirement> skills = new ArrayList<>();

        if(lower.contains("java")){

            skills.add(
                    new SkillRequirement(
                            "java",
                            30,
                            true
                    )
            );
        }


        if(lower.contains("spring")){

            skills.add(
                    new SkillRequirement(
                            "spring",
                            30,
                            true
                    )
            );
        }


        if(lower.contains("aws")){

            skills.add(
                    new SkillRequirement(
                            "aws",
                            10,
                            false
                    )
            );
        }


        if(lower.contains("kubernetes")){

            skills.add(
                    new SkillRequirement(
                            "kubernetes",
                            10,
                            false
                    )
            );
        }

        return skills;


    }
}
