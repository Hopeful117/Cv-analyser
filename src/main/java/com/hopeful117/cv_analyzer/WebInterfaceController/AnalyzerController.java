package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.model.ResumeAnalysisResult;
import com.hopeful117.cv_analyzer.service.AnalysisFace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class AnalyzerController {
    private final AnalysisFace analysisFace;

    @GetMapping("/analyze")
    public String getAnalyzer(){
        return "analyzer";
    }

    @PostMapping("/analyze")
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String jobOffer,
            @RequestParam(required = false) String jobOfferUrl,
            Model model) throws Exception {

        ResumeAnalysisResult result =
                analysisFace.analyze(
                        file,
                        jobOffer,
                        jobOfferUrl
                );

        model.addAttribute(
                "analysis",
                result.analysis()
        );
        model.addAttribute(
                "generatedResume",
                result.generatedResume()
        );

        return "result-analyzer";
    }
}
