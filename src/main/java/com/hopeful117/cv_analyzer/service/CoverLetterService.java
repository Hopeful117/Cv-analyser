package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.generator.AiCoverLetterGenerator;
import com.hopeful117.cv_analyzer.helper.ResolveOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CoverLetterService {
    private final AiCoverLetterGenerator aiCoverLetterGenerator;
    private final PdfParserService pdfParserService;
    private final ResolveOffer resolveOffer;

    public String generateCoverLetter(MultipartFile letter, MultipartFile cv, String jobOfferText,String jobOfferUrl) throws IOException {
        String letterText="";
        if(!letter.isEmpty()) {
            letterText= pdfParserService.extractText(letter);

        }
       String cvText= pdfParserService.extractText(cv);
       String offerText = resolveOffer.resolveOffer(jobOfferText, jobOfferUrl);
       return generateFromTexts(letterText, cvText, offerText);
    }

    public String generateFromTexts(String existingLetter, String cvText, String offerText) {
        return aiCoverLetterGenerator.generateCoverLetter(existingLetter, cvText, offerText);
    }
}
