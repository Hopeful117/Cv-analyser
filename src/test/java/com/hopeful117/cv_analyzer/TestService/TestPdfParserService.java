package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.service.PdfParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class TestPdfParserService {

    private PdfParserService pdfParserService;

    @BeforeEach
    void setUp() {
        pdfParserService = new PdfParserService();
    }

    @Test
    void shouldExtractTextFromPdf() throws IOException {
        // Charger le fichier PDF de sample
        String samplePdfPath = "src/main/resources/sample/CV.pdf";
        byte[] pdfContent = Files.readAllBytes(Paths.get(samplePdfPath));

        // Créer un MockMultipartFile avec le contenu du PDF
        MultipartFile mockFile = new MockMultipartFile(
            "file",
            "CV.pdf",
            "application/pdf",
            pdfContent
        );

        // Extraire le texte du PDF
        String extractedText = pdfParserService.extractText(mockFile);

        // Vérifications
        assertThat(extractedText)
            .isNotNull()
            .isNotEmpty()
            .hasSizeGreaterThan(0);
    }

    @Test
    void shouldThrowExceptionForInvalidPdf() {
        // Créer un fichier invalide (pas un PDF)
        byte[] invalidContent = "Ceci n'est pas un PDF".getBytes();
        MultipartFile invalidFile = new MockMultipartFile(
            "file",
            "invalid.pdf",
            "application/pdf",
            invalidContent
        );

        // Vérifier que l'exception est bien lancée
        assertThatThrownBy(() -> pdfParserService.extractText(invalidFile))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Failed to parse PDF file");
    }

    @Test
    void shouldHandleEmptyFile() {
        // Créer un fichier vide
        byte[] emptyContent = new byte[0];
        MultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            emptyContent
        );

        // Vérifier que l'exception est bien lancée pour un fichier vide
        assertThatThrownBy(() -> pdfParserService.extractText(emptyFile))
            .isInstanceOf(IOException.class);
    }
}
