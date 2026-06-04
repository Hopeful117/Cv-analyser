package com.hopeful117.cv_analyzer.controller;

import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import com.hopeful117.cv_analyzer.service.AnalysisFace;
import com.hopeful117.cv_analyzer.service.JobScrapperService;
import com.hopeful117.cv_analyzer.service.PdfParserService;
import com.hopeful117.cv_analyzer.service.ResumeAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final AnalysisFace analysisFace;
    @GetMapping("/")
    public String home() {
        return "index";
    }
    @PostMapping("/analyze")
    public String analyze(Model model,MultipartFile file, String jobOffer,String jobOfferUrl) throws Exception {

        ResumeAnalysis analysis= analysisFace.analyze(file,jobOffer,jobOfferUrl);

        model.addAttribute(
                "analysis",
                analysis
        );

        return "result";
    }
}
