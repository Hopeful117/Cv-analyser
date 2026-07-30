package com.hopeful117.cv_analyzer.career.application;

import com.hopeful117.cv_analyzer.exception.InvalidUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
public class UploadValidationService {
    private static final long MAX_PDF_SIZE = 5L * 1024 * 1024;

    public void requirePdf(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadException(label + " est requis.");
        }
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new InvalidUploadException(label + " dépasse la taille maximale de 5 Mo.");
        }
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        boolean pdfName = filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean pdfType = contentType == null || contentType.equalsIgnoreCase("application/pdf");
        if (!pdfName || !pdfType) {
            throw new InvalidUploadException(label + " doit être un fichier PDF.");
        }
    }

    public void validateOptionalPdf(MultipartFile file, String label) {
        if (file != null && !file.isEmpty()) {
            requirePdf(file, label);
        }
    }
}
