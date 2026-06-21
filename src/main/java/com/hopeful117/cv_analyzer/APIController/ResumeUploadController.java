package com.hopeful117.cv_analyzer.APIController;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.AnalysisFace;
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
   private AnalysisFace analysisFace;

    @PostMapping("/analyze-upload")
    public ResponseEntity<ResumeAnalysis> analyzeUpload(@RequestParam("file")MultipartFile file,
                                                        @RequestParam("jobOffer") String jobOfferText) throws Exception {
        ResumeAnalysis analysis= analysisFace.analyze(file,jobOfferText,null);
        return ResponseEntity.ok(analysis);
    }
}
