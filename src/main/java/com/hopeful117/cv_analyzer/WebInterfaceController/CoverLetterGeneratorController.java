package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class CoverLetterGeneratorController {
    private final CareerWorkspaceService careerWorkspaceService;
    @GetMapping("/generator")
    public String getGenerator(){
        return "generator";
    }
    @PostMapping("/generator")
    public String generateCoverLetter(
            @RequestParam("LetterFile") MultipartFile LetterFile,
            @RequestParam("CvFile") MultipartFile CvFile,
            @RequestParam(required = false) String jobOffer,
            @RequestParam(required = false) String jobOfferUrl,
            @RequestParam(required = false) String opportunityTitle,
            @RequestParam(required = false) String companyName) {
        Long id = careerWorkspaceService.generateCoverLetter(
                LetterFile, CvFile, jobOffer, jobOfferUrl, opportunityTitle, companyName);
        return "redirect:/cover-letters/" + id;
    }
}
