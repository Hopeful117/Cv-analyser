package com.hopeful117.cv_analyzer.controller;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResumeUploadController {
    private final PdfParserService pdfParserService;
    private final ResumeAnalysisService resumeAnalysisService;

    @PostMapping("/analyze-upload")
    public ResponseEntity<ResumeAnalysis> analyzeUpload(@RequestParam("file")MultipartFile file,
                                                        @RequestParam("jobOffer") String jobOfferText) throws Exception {
        String resumeText = pdfParserService.extractText(file);
        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resumeText, jobOfferText);
        return ResponseEntity.ok(analysis);
    }
}
