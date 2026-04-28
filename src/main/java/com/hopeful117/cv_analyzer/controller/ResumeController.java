package com.hopeful117.cv_analyzer.controller;

import com.hopeful117.cv_analyzer.model.AnalysisRequest;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.JobScrapperService;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResumeController {
    private final ResumeAnalysisService resumeAnalysisService;
    private final JobScrapperService jobScrapperService;

    @PostMapping("/analyze")
    public ResumeAnalysis analyze(@RequestBody AnalysisRequest analysisRequest) throws IOException {
        String offerText= jobScrapperService.extractTextFromUrl(analysisRequest.getJobOfferUrl());
        return resumeAnalysisService.analyzeResume(analysisRequest.getResumeText(), offerText);
    }



}
