package com.hopeful117.cv_analyzer.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfParserService {
    public String extractText(MultipartFile file) throws IOException {
        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();
            return text;
        } catch (IOException e) {
            throw new IOException("Failed to parse PDF file: " + e.getMessage(), e);
        }
    }

}
