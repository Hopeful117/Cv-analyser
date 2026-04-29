package com.hopeful117.cv_analyzer.TestAnalyzer;


import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.extractor.KeywordExtractor;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.model.SkillRequirement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class TestResumeAnalyzer {

    @Mock
    private KeywordExtractor keywordExtractor;

    private ResumeAnalyzer analyzer;

    @Test
    void shouldDetectMissingKeywords() {
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring SQL developer";
        String offer = "Java Spring SQL Docker AWS Kubernetes developer";
        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5,true),
                new SkillRequirement("spring", 4,true),
                new SkillRequirement("sql", 3,true),
                new SkillRequirement("docker", 2,false),
                new SkillRequirement("aws", 1,false),
                new SkillRequirement("kubernetes", 1,false)
        );

        // Mock du comportement de l'extracteur
        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);

        assertThat(result.getMissingKeywords())
                .contains("docker", "aws", "kubernetes");

        assertThat(result.getJobMatchScore())
                .isLessThan(100);
    }


    @Test
    void shouldGivePerfectMatchWhenAllKeywordsPresent(){
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring SQL Docker AWS Kubernetes";
        String offer = "Java Spring SQL Docker AWS Kubernetes";

        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5,true),
                new SkillRequirement("spring", 4,true),
                new SkillRequirement("sql", 3,true),
                new SkillRequirement("docker", 2,false),
                new SkillRequirement("aws", 1,false),
                new SkillRequirement("kubernetes", 1,false)
        );


        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);

        assertThat(result.getMissingKeywords())
                .isEmpty();

        assertThat(result.getJobMatchScore())
                .isEqualTo(100);
    }


    @Test
    void shouldPenalizeShortResume(){
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring";
        String offer = "Java Spring SQL";

        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5,true),
                new SkillRequirement("spring", 4,true),
                new SkillRequirement("sql", 3,true)
        );


        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);

        assertThat(result.getCvQualityScore())
                .isLessThan(100);

        assertThat(result.getAtsRisks())
                .contains("CV trop court.");
    }


    @Test
    void overallScoreShouldBeAverage(){
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring SQL";
        String offer = "Java Spring SQL Docker";

        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5,true),
                new SkillRequirement("spring", 4,true),
                new SkillRequirement("sql", 3,true),
                new SkillRequirement("docker", 2,false)
        );


        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);

        int expectedAverage = (result.getCvQualityScore() + result.getAtsScore() + result.getJobMatchScore()) / 3;

        assertThat(result.getOverallScore()).isEqualTo(expectedAverage);
    }

    @Test
    void shouldApplyWeightingToJobMatchScore(){
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring"; // Only has Java and Spring
        String offer = "Java Spring SQL Docker";


        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5, true),     // Found - high weight
                new SkillRequirement("spring", 4, true),   // Found - medium weight
                new SkillRequirement("sql", 3, true),      // Missing - medium weight
                new SkillRequirement("docker", 1, false)   // Missing - low weight
        );

        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);


        assertThat(result.getJobMatchScore())
                .isEqualTo(69);  // Exact calculation with weighting

        assertThat(result.getMissingKeywords())
                .contains("sql", "docker");
    }

    @Test
    void shouldPrioritizeMandatorySkills(){
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Docker AWS"; // Has non-mandatory skills
        String offer = "Java Spring Docker AWS";

        List<SkillRequirement> requirements = List.of(
                new SkillRequirement("java", 5, true),      // Missing - mandatory, high weight
                new SkillRequirement("spring", 4, true),    // Missing - mandatory, high weight
                new SkillRequirement("docker", 1, false),   // Found - non-mandatory, low weight
                new SkillRequirement("aws", 1, false)       // Found - non-mandatory, low weight
        );

        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(requirements);

        ResumeAnalysis result = analyzer.analyze(resume, offer);


        assertThat(result.getJobMatchScore())
                .isEqualTo(18);  // Low score due to missing mandatory skills

        assertThat(result.getMissingKeywords())
                .contains("java", "spring");
    }
}
