package com.hopeful117.cv_analyzer.TestAnalyzer;


import com.hopeful117.cv_analyzer.analyzer.ResumeAnalyzer;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestResumeAnalyzer {
    private final ResumeAnalyzer analyzer = new ResumeAnalyzer();

    @Test
    void shouldDetectMissingKeywords() {

        String resume =
                "Java Spring SQL developer";

        String offer =
                "Java Spring SQL Docker AWS Kubernetes developer";


        ResumeAnalysis result =
                analyzer.analyze(resume, offer);


        assertThat(result.getMissingKeywords())
                .contains("docker","aws","kubernetes");

        assertThat(result.getJobMatchScore())
                .isLessThan(100);
    }


    @Test
    void shouldGivePerfectMatchWhenAllKeywordsPresent(){

        String resume =
                "Java Spring SQL Docker AWS Kubernetes";

        String offer =
                "Java Spring SQL Docker AWS Kubernetes";


        ResumeAnalysis result =
                analyzer.analyze(resume, offer);


        assertThat(result.getMissingKeywords())
                .isEmpty();

        assertThat(result.getJobMatchScore())
                .isEqualTo(100);
    }


    @Test
    void shouldPenalizeShortResume(){

        String resume =
                "Java Spring";

        String offer =
                "Java Spring SQL";


        ResumeAnalysis result =
                analyzer.analyze(resume, offer);


        assertThat(result.getCvQualityScore())
                .isLessThan(100);

        assertThat(result.getAtsRisks())
                .contains("CV trop court.");
    }


    @Test
    void overallScoreShouldBeAverage(){

        String resume =
                "Java Spring SQL";

        String offer =
                "Java Spring SQL Docker";


        ResumeAnalysis result =
                analyzer.analyze(resume, offer);


        int expectedAverage =
                (
                        result.getCvQualityScore()
                                +result.getAtsScore()
                                +result.getJobMatchScore()
                ) / 3;


        assertThat(
                result.getOverallScore()
        ).isEqualTo(expectedAverage);
    }

}
