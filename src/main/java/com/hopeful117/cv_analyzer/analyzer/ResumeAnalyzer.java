package com.hopeful117.cv_analyzer.analyzer;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ResumeAnalyzer {
    public ResumeAnalysis analyze(String resumeText, String jobOfferText) {
     AtomicInteger score= new AtomicInteger(100);
     List<String> risks = new ArrayList<>();
     List<String> recommendations = new ArrayList<>();
     if (resumeText.length() < 300) {
         score.addAndGet(-20);
         risks.add("Resume is too short, may not provide enough information.");
         recommendations.add("Consider adding more details about your experience and skills.");
     }
     List<String> keywords = List.of("Java", "Spring", "SQL", "AWS");
     keywords.forEach(keyword -> {;
         if (!resumeText.contains(keyword)) {
             score.addAndGet(-10);
             risks.add("Missing keyword: " + keyword);
             recommendations.add("Include relevant keywords from the job offer to improve ATS compatibility.");
         }
     });
     score.set(Math.max(score.get(), 0));
     final int finalScore = score.get();
     return new ResumeAnalysis(finalScore, risks, recommendations);
    }



}
