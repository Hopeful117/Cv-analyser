package com.hopeful117.cv_analyzer.analyzer;

import com.hopeful117.cv_analyzer.extractor.JobKeyWordExtractor;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
public class ResumeAnalyzer {
    private final JobKeyWordExtractor keywordExtractor;
    public ResumeAnalysis analyze(String resumeText, String jobOfferText) {
        List<String> risks = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();
        String resume = resumeText.toLowerCase();
        String offer = jobOfferText.toLowerCase();
        int cvScore = scoreCV(
                resume,
                risks,
                recommendations
        );

        int atsScore = scoreATS(
                resume,
                risks
        );

        int matchScore = scoreJobMatch(
                resume,
                offer,
                missingKeywords,
                recommendations
        );


        int overallScore =
                (cvScore + atsScore + matchScore) / 3;


        return new ResumeAnalysis(
                overallScore,
                cvScore,
                atsScore,
                matchScore,
                risks,
                recommendations,
                missingKeywords
        );
    }


    private int scoreCV(
            String resume,
            List<String> risks,
            List<String> recs){

        AtomicInteger score =
                new AtomicInteger(100);

        if(resume.length() < 300){

            score.addAndGet(-20);

            risks.add(
                    "CV trop court."
            );

            recs.add(
                    "Ajouter davantage d'expérience."
            );
        }

        return Math.max(score.get(),0);
    }


    private int scoreATS(
            String resume,
            List<String> risks){

        int score = 100;

        if(resume.contains("table")){

            score -= 20;

            risks.add(
                    "Tableaux potentiellement problématiques ATS"
            );
        }

        return Math.max(score,0);
    }



    private int scoreJobMatch(
            String resume,
            String offer,
            List<String> missing,
            List<String> recs){

        List<String> extractedKeywords =
                extractKeywords(offer);

        int found = 0;

        for(String keyword : extractedKeywords){

            if(resume.contains(keyword)){

                found++;

            } else {

                missing.add(keyword);

            }

        }

        if(!missing.isEmpty()){

            recs.add(
                    "Ajouter certains mots-clés présents dans l'annonce."
            );
        }

        return extractedKeywords.isEmpty()
                ? 100
                : (found * 100 / extractedKeywords.size());
    }



    private List<String> extractKeywords(
            String offer){
        return keywordExtractor.extract(offer);



    }

}
