package com.hopeful117.cv_analyzer.extractor;

import com.hopeful117.cv_analyzer.model.SkillRequirement;

import java.util.List;

public interface KeywordExtractor {
    List<SkillRequirement> extract(String text);
}
