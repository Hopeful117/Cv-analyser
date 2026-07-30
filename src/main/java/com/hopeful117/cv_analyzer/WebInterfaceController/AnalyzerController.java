package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class AnalyzerController {
    private final CareerWorkspaceService careerWorkspaceService;

    @GetMapping("/analyze")
    public String getAnalyzer(){
        return "analyzer";
    }

    @PostMapping("/analyze")
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String jobOffer,
            @RequestParam(required = false) String jobOfferUrl,
            @RequestParam(required = false) String opportunityTitle,
            @RequestParam(required = false) String companyName) {
        Long analysisId = careerWorkspaceService.analyze(
                file, jobOffer, jobOfferUrl, opportunityTitle, companyName);
        return "redirect:/analyses/" + analysisId;
    }
}
