package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import com.hopeful117.cv_analyzer.service.ResumePdfService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestResumePdfService {
    private ResumePdfService resumePdfService;

    @BeforeEach
    void setUp() {
        resumePdfService = new ResumePdfService();
    }

    @Test
    void shouldGenerateReadablePdfForEveryStyle() throws IOException {
        String content = """
                JOHN DOE

                PROFESSIONAL EXPERIENCE
                Java Developer - Example Company
                Developed Spring applications.

                SKILLS
                Java, Spring, SQL
                """;

        for (ResumePdfStyle style : ResumePdfStyle.values()) {
            byte[] pdf = resumePdfService.generate(content, style);

            assertThat(pdf).startsWith("%PDF".getBytes());
            try (PDDocument document = Loader.loadPDF(pdf)) {
                String extractedText = new PDFTextStripper().getText(document);
                assertThat(extractedText)
                        .contains("JOHN DOE")
                        .contains("Developed Spring applications.");
            }
        }
    }

    @Test
    void shouldAlwaysGenerateExactlyOnePage() throws IOException {
        String content = "PROFESSIONAL EXPERIENCE\n"
                + "Java Developer - Developed Spring applications.\n".repeat(45);

        byte[] pdf = resumePdfService.generate(content, ResumePdfStyle.PROFESSIONAL);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void shouldRejectContentThatCannotFitOnOneReadablePage() {
        String content = "PROFESSIONAL EXPERIENCE\n"
                + "Java Developer - Developed Spring applications and maintained complex systems.\n"
                .repeat(160);

        assertThatThrownBy(() ->
                resumePdfService.generate(content, ResumePdfStyle.PROFESSIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("une page A4 lisible");
    }

    @Test
    void shouldRejectBlankResume() {
        assertThatThrownBy(() ->
                resumePdfService.generate("   ", ResumePdfStyle.MINIMAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne peut pas être vide");
    }

    @Test
    void shouldReplaceCharactersUnsupportedByHelvetica() throws IOException {
        String content = """
                SKILLS
                Backend ↔ Frontend → Platform ✓ 🚀
                """;

        byte[] pdf = resumePdfService.generate(content, ResumePdfStyle.MODERN);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extractedText = new PDFTextStripper().getText(document);
            assertThat(extractedText)
                    .contains("Backend <-> Frontend -> Platform [x] ?")
                    .doesNotContain("↔", "🚀");
        }
    }

    @Test
    void shouldRenderVisuallyDistinctTemplates() throws IOException {
        String content = """
                JOHN DOE
                Software Engineer

                EXPERIENCE
                Java Developer | Example Company | 2020 - Present
                Location: Paris
                - Developed Spring applications.

                SKILLS
                Java, Spring, SQL
                """;

        int professionalRectangles = countRectangleOperators(
                resumePdfService.generate(content, ResumePdfStyle.PROFESSIONAL));
        int modernRectangles = countRectangleOperators(
                resumePdfService.generate(content, ResumePdfStyle.MODERN));
        int minimalRectangles = countRectangleOperators(
                resumePdfService.generate(content, ResumePdfStyle.MINIMAL));

        assertThat(professionalRectangles).isEqualTo(2);
        assertThat(modernRectangles).isGreaterThan(professionalRectangles);
        assertThat(minimalRectangles).isZero();
    }

    @Test
    void shouldUseBoldItalicAndSectionSeparators() throws IOException {
        String content = """
                JOHN DOE
                Software Engineer

                Professional Experience
                Java Developer | Example Company | 2020 - Present
                Location: Paris
                - Developed Spring applications.
                """;

        byte[] pdf = resumePdfService.generate(content, ResumePdfStyle.PROFESSIONAL);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            var page = document.getPage(0);
            int fontCount = 0;
            for (var ignored : page.getResources().getFontNames()) {
                fontCount++;
            }

            PDFStreamParser parser = new PDFStreamParser(page);
            long separatorCount = parser.parse().stream()
                    .filter(Operator.class::isInstance)
                    .map(Operator.class::cast)
                    .filter(operator -> "l".equals(operator.getName()))
                    .count();

            assertThat(fontCount).isGreaterThanOrEqualTo(3);
            assertThat(separatorCount).isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void shouldNotUseContactSectionAsCandidateName() throws IOException {
        String content = """
                CONTACT
                LUDOVIC BROT
                Phone: 06.76.15.65.16 | Email: ludovic.brot@gmail.com | Location: Bourges

                TITLE
                JAVA DEVELOPER / SPRING BOOT JUNIOR

                SUMMARY
                Developer specialized in Java and Spring Boot.

                EXPERIENCE
                [TO COMPLETE: professional experience entries]

                EDUCATION
                Application Developer | OpenClassrooms | 2025 - 2026

                SKILLS
                - Technical: Java, Spring Boot, PostgreSQL, Docker
                """;

        for (ResumePdfStyle style : ResumePdfStyle.values()) {
            byte[] pdf = resumePdfService.generate(content, style);

            try (PDDocument document = Loader.loadPDF(pdf)) {
                String extractedText = new PDFTextStripper().getText(document).strip();

                assertThat(extractedText).startsWith("LUDOVIC BROT");
                assertThat(extractedText.indexOf("LUDOVIC BROT"))
                        .isLessThan(extractedText.indexOf("CONTACT"));
                assertThat(extractedText)
                        .contains("JAVA DEVELOPER / SPRING BOOT JUNIOR")
                        .doesNotContain("\nTITLE\n");
            }
        }
    }

    @Test
    void shouldUseStructuredIdentityRegardlessOfSectionLanguage() throws IOException {
        String content = """
                KONTAKT
                LUDOVIC BROT
                Telefon: 06.76.15.65.16 | E-Mail: ludovic.brot@gmail.com

                BERUFSTITEL
                JAVA-ENTWICKLER

                ZUSAMMENFASSUNG
                Entwickler mit Schwerpunkt Java und Spring Boot.

                KENNTNISSE
                - Java
                - Spring Boot
                """;

        byte[] pdf = resumePdfService.generate(
                content,
                ResumePdfStyle.PROFESSIONAL,
                "LUDOVIC BROT",
                "JAVA-ENTWICKLER"
        );

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extractedText = new PDFTextStripper().getText(document).strip();

            assertThat(extractedText).startsWith("LUDOVIC BROT");
            assertThat(countOccurrences(extractedText, "LUDOVIC BROT")).isEqualTo(1);
            assertThat(countOccurrences(extractedText, "JAVA-ENTWICKLER")).isEqualTo(1);
            assertThat(extractedText).contains("KONTAKT", "ZUSAMMENFASSUNG", "KENNTNISSE");
        }
    }

    private int countRectangleOperators(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFStreamParser parser = new PDFStreamParser(document.getPage(0));
            return (int) parser.parse().stream()
                    .filter(Operator.class::isInstance)
                    .map(Operator.class::cast)
                    .filter(operator -> "re".equals(operator.getName()))
                    .count();
        }
    }

    private int countOccurrences(String text, String value) {
        return text.split(Pattern.quote(value), -1).length - 1;
    }
}
