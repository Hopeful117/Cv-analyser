package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.service.CoverLetterService;
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
    private final CoverLetterService coverLetterService;
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
            Model model) throws IOException {

        String letter = coverLetterService.generateCoverLetter(
                LetterFile,
                CvFile,
                jobOffer,
                jobOfferUrl
        );
        model.addAttribute(
                "coverLetter",
                letter
        );
        return "result-generator";


    }
}
