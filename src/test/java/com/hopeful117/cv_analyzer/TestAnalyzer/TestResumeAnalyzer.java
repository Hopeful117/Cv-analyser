package com.hopeful117.cv_analyzer.TestAnalyzer;


import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.extractor.JobKeyWordExtractor;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
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
    private JobKeyWordExtractor keywordExtractor;

    private ResumeAnalyzer analyzer;

    @Test
    void shouldDetectMissingKeywords() {
        analyzer = new ResumeAnalyzer(keywordExtractor);

        String resume = "Java Spring SQL developer";
        String offer = "Java Spring SQL Docker AWS Kubernetes developer";

        // Mock du comportement de l'extracteur
        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(List.of("java", "spring", "sql", "docker", "aws", "kubernetes"));

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

        // Mock du comportement de l'extracteur
        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(List.of("java", "spring", "sql", "docker", "aws", "kubernetes"));

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

        // Mock du comportement de l'extracteur
        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(List.of("java", "spring", "sql"));

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

        // Mock du comportement de l'extracteur
        Mockito.when(keywordExtractor.extract(offer.toLowerCase()))
                .thenReturn(List.of("java", "spring", "sql", "docker"));

        ResumeAnalysis result = analyzer.analyze(resume, offer);

        int expectedAverage = (result.getCvQualityScore() + result.getAtsScore() + result.getJobMatchScore()) / 3;

        assertThat(result.getOverallScore()).isEqualTo(expectedAverage);
    }

}
