package com.hopeful117.cv_analyzer.extractor;

import com.hopeful117.cv_analyzer.model.SkillRequirement;

public interface ImportanceDetector {
    SkillRequirement detect(String keyword, String jobOfferText);
}
