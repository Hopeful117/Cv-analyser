package com.hopeful117.cv_analyzer.TestExtractor;

import com.hopeful117.cv_analyzer.extractor.ImportanceDetector;
import com.hopeful117.cv_analyzer.extractor.JobKeyWordExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class TestKeywordExtractor {
    @Mock
    private ImportanceDetector importanceDetector;
    private JobKeyWordExtractor keywordExtractor;
    private static final Set<String> TECH_KEYWORDS = Set.of(
            "java","spring","aws","docker",
            "kubernetes","sql","api","git",
            "react","python","django"
    );
    @Test
    void shouldExtractKeywordsFromText(){
        keywordExtractor = new JobKeyWordExtractor(importanceDetector);
        String text = "We are looking for a Java Spring developer with experience in AWS and Docker.";
        var skills = keywordExtractor.extract(text);
        assert(skills.size() == 4);
    }

    @Test
    void shouldReturnEmptyListWhenNoKeywords(){
        keywordExtractor = new JobKeyWordExtractor(importanceDetector);
        String text = "We are looking for a designer with experience in Photoshop and Illustrator.";
        var skills = keywordExtractor.extract(text);
        assert(skills.isEmpty());
    }





}
