package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ResumePdfService {
    private static final float PAGE_MARGIN = 48;
    private static final float BOTTOM_MARGIN = 48;
    private static final float DEFAULT_BODY_SIZE = 10.8f;
    private static final float MINIMUM_BODY_SIZE = 8.5f;
    private static final Pattern DATE_PATTERN = Pattern.compile(
            ".*\\b(?:19|20)\\d{2}\\b.*|.*\\b(?:present|current|today|présent|actuel|aujourd'hui)\\b.*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> SECTION_NAMES = Set.of(
            "profile", "professional profile", "summary", "professional summary", "objective",
            "experience", "professional experience", "work experience", "employment",
            "education", "training", "skills", "technical skills", "soft skills",
            "certifications", "projects", "languages", "interests", "contact",
            "title", "professional title", "headline",
            "profil", "profil professionnel", "résumé", "objectif",
            "expérience", "expériences", "expérience professionnelle", "expériences professionnelles",
            "formation", "formations", "compétences", "compétences techniques",
            "projets", "langues", "centres d'intérêt", "coordonnées",
            "titre", "titre professionnel",
            "erfahrung", "berufserfahrung", "ausbildung", "kenntnisse", "sprachen",
            "experiencia", "formación", "habilidades", "idiomas"
    );

    public byte[] generate(String resumeContent, ResumePdfStyle style) throws IOException {
        return generate(resumeContent, style, null, null);
    }

    public byte[] generate(
            String resumeContent,
            ResumePdfStyle style,
            String candidateName,
            String professionalTitle
    ) throws IOException {
        if (resumeContent == null || resumeContent.isBlank()) {
            throw new IllegalArgumentException("Le contenu du CV ne peut pas être vide.");
        }

        ResumePdfStyle selectedStyle = style == null ? ResumePdfStyle.PROFESSIONAL : style;

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRenderer renderer = new PdfRenderer(
                    document,
                    selectedStyle,
                    candidateName,
                    professionalTitle
            );
            renderer.render(resumeContent);
            document.save(output);
            return output.toByteArray();
        }
    }

    private static final class PdfRenderer {
        private final PDDocument document;
        private final ResumePdfStyle style;
        private final PDFont regularFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont boldFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final PDFont italicFont =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        private PDPage page;
        private PDPageContentStream stream;
        private float cursorY;
        private float bodyFontSize = DEFAULT_BODY_SIZE;
        private ResumeLayout layout;
        private final String providedCandidateName;
        private final String providedProfessionalTitle;

        private PdfRenderer(
                PDDocument document,
                ResumePdfStyle style,
                String candidateName,
                String professionalTitle
        ) {
            this.document = document;
            this.style = style;
            this.providedCandidateName = candidateName;
            this.providedProfessionalTitle = professionalTitle;
        }

        private void render(String content) throws IOException {
            layout = parseLayout(content);
            bodyFontSize = findReadableFontSize(layout.body());
            addPage();
            drawHeader();

            for (String paragraph : layout.body()) {
                renderParagraph(paragraph);
            }

            closeStream();
        }

        private void addPage() throws IOException {
            closeStream();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);

            if (style == ResumePdfStyle.PROFESSIONAL) {
                stream.setNonStrokingColor(new PDColor(
                        new float[]{0.07f, 0.16f, 0.29f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.addRect(0, page.getMediaBox().getHeight() - 108,
                        page.getMediaBox().getWidth(), 108);
                stream.fill();
                stream.setNonStrokingColor(new PDColor(
                        new float[]{0.82f, 0.63f, 0.20f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.addRect(0, page.getMediaBox().getHeight() - 108,
                        page.getMediaBox().getWidth(), 5);
                stream.fill();
            } else if (style == ResumePdfStyle.MODERN) {
                stream.setNonStrokingColor(new PDColor(
                        new float[]{0.02f, 0.55f, 0.62f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.addRect(0, 0, 16, page.getMediaBox().getHeight());
                stream.fill();
            }
        }

        private void drawHeader() throws IOException {
            switch (style) {
                case PROFESSIONAL -> drawProfessionalHeader();
                case MODERN -> drawModernHeader();
                case MINIMAL -> drawMinimalHeader();
            }
        }

        private void drawProfessionalHeader() throws IOException {
            drawText(layout.name(), boldFont, 23, PAGE_MARGIN, 786, white());
            if (layout.subtitle() != null) {
                drawText(layout.subtitle(), italicFont, 11.5f, PAGE_MARGIN, 760,
                        new PDColor(new float[]{0.86f, 0.90f, 0.95f}, PDDeviceRGB.INSTANCE));
            }
            cursorY = 716;
        }

        private void drawModernHeader() throws IOException {
            drawText(layout.name(), boldFont, 23, 50, 790,
                    new PDColor(new float[]{0.06f, 0.16f, 0.23f}, PDDeviceRGB.INSTANCE));
            if (layout.subtitle() != null) {
                drawText(layout.subtitle(), boldFont, 11.5f, 50, 765,
                        new PDColor(new float[]{0.02f, 0.48f, 0.56f}, PDDeviceRGB.INSTANCE));
            }
            stream.setStrokingColor(new PDColor(
                    new float[]{0.02f, 0.55f, 0.62f},
                    PDDeviceRGB.INSTANCE
            ));
            stream.setLineWidth(2.2f);
            stream.moveTo(50, 748);
            stream.lineTo(page.getMediaBox().getWidth() - PAGE_MARGIN, 748);
            stream.stroke();
            cursorY = 728;
        }

        private void drawMinimalHeader() throws IOException {
            drawCenteredText(layout.name(), boldFont, 21, 793,
                    new PDColor(new float[]{0.08f, 0.08f, 0.08f}, PDDeviceRGB.INSTANCE));
            if (layout.subtitle() != null) {
                drawCenteredText(layout.subtitle(), italicFont, 10.5f, 769,
                        new PDColor(new float[]{0.38f, 0.38f, 0.38f}, PDDeviceRGB.INSTANCE));
            }
            stream.setStrokingColor(new PDColor(
                    new float[]{0.72f, 0.72f, 0.72f},
                    PDDeviceRGB.INSTANCE
            ));
            stream.setLineWidth(0.6f);
            stream.moveTo(110, 750);
            stream.lineTo(page.getMediaBox().getWidth() - 110, 750);
            stream.stroke();
            cursorY = 728;
        }

        private void renderParagraph(String rawParagraph) throws IOException {
            String paragraph = sanitize(rawParagraph.strip());
            if (paragraph.isEmpty()) {
                cursorY -= 7;
                return;
            }

            TextRole role = classify(paragraph);
            boolean heading = role == TextRole.SECTION;
            PDFont font = fontFor(role);
            float fontSize = fontSizeFor(role);
            float lineHeight = fontSize * 1.35f;
            float availableWidth = page.getMediaBox().getWidth() - (2 * PAGE_MARGIN);

            if (heading) {
                cursorY -= 4;
            }

            for (String line : wrap(paragraph, font, fontSize, availableWidth)) {
                ensureSpace(lineHeight);
                drawLine(line, font, fontSize, role);
                cursorY -= lineHeight;
            }

            cursorY -= switch (role) {
                case SECTION -> 5;
                case ENTRY_TITLE -> 3;
                default -> 1;
            };
        }

        private void drawLine(String text, PDFont font, float fontSize, TextRole role)
                throws IOException {
            boolean heading = role == TextRole.SECTION;
            float textX = style == ResumePdfStyle.MODERN ? 50 : PAGE_MARGIN;
            if (role == TextRole.BULLET) {
                textX += 12;
            }

            if (heading && style == ResumePdfStyle.MODERN) {
                stream.setNonStrokingColor(new PDColor(
                        new float[]{0.88f, 0.96f, 0.97f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.addRect(42, cursorY - 5,
                        page.getMediaBox().getWidth() - 42 - PAGE_MARGIN,
                        fontSize + 9);
                stream.fill();
                textX = 50;
            }

            drawText(text, font, fontSize, textX, cursorY, textColor(role));

            if (heading && style == ResumePdfStyle.PROFESSIONAL) {
                stream.setStrokingColor(new PDColor(
                        new float[]{0.82f, 0.63f, 0.20f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.setLineWidth(1.3f);
                stream.moveTo(PAGE_MARGIN, cursorY - 4);
                stream.lineTo(page.getMediaBox().getWidth() - PAGE_MARGIN, cursorY - 4);
                stream.stroke();
            } else if (heading && style == ResumePdfStyle.MINIMAL) {
                stream.setStrokingColor(new PDColor(
                        new float[]{0.78f, 0.78f, 0.78f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.setLineWidth(0.5f);
                stream.moveTo(PAGE_MARGIN, cursorY - 4);
                stream.lineTo(page.getMediaBox().getWidth() - PAGE_MARGIN, cursorY - 4);
                stream.stroke();
            }

            if (role == TextRole.ENTRY_TITLE) {
                stream.setStrokingColor(new PDColor(
                        new float[]{0.86f, 0.86f, 0.86f},
                        PDDeviceRGB.INSTANCE
                ));
                stream.setLineWidth(0.35f);
                stream.moveTo(textX, cursorY - 4);
                stream.lineTo(page.getMediaBox().getWidth() - PAGE_MARGIN, cursorY - 4);
                stream.stroke();
            }
        }

        private void drawText(
                String text,
                PDFont font,
                float fontSize,
                float x,
                float y,
                PDColor color
        ) throws IOException {
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(x, y);
            stream.showText(text);
            stream.endText();
        }

        private void drawCenteredText(
                String text,
                PDFont font,
                float fontSize,
                float y,
                PDColor color
        ) throws IOException {
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            float x = (page.getMediaBox().getWidth() - textWidth) / 2;
            drawText(text, font, fontSize, x, y, color);
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (cursorY - requiredHeight < BOTTOM_MARGIN) {
                throw new IllegalArgumentException(
                        "Le contenu est trop long pour tenir sur une page A4 lisible. "
                                + "Raccourcissez le CV avant de générer le PDF."
                );
            }
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        private boolean isHeading(String paragraph) {
            if (paragraph.length() > 70) {
                return false;
            }

            String letters = paragraph.replaceAll("[^\\p{L}]", "");
            return isKnownSection(paragraph)
                    || paragraph.endsWith(":")
                    || (!letters.isEmpty() && letters.equals(letters.toUpperCase()));
        }

        private boolean isKnownSection(String paragraph) {
            return SECTION_NAMES.contains(normalizeLabel(paragraph));
        }

        private String normalizeLabel(String value) {
            return value.replaceAll("[:\\s]+$", "")
                    .strip()
                    .toLowerCase(Locale.ROOT);
        }

        private TextRole classify(String paragraph) {
            if (isHeading(paragraph)) {
                return TextRole.SECTION;
            }
            if (paragraph.startsWith("-")) {
                return TextRole.BULLET;
            }
            if (looksLikeContactDetails(paragraph)) {
                return TextRole.METADATA;
            }
            if (paragraph.contains("|")
                    || (paragraph.contains(" - ") && DATE_PATTERN.matcher(paragraph).matches())) {
                return TextRole.ENTRY_TITLE;
            }
            if (DATE_PATTERN.matcher(paragraph).matches()
                    || paragraph.toLowerCase(Locale.ROOT).startsWith("location:")
                    || paragraph.toLowerCase(Locale.ROOT).startsWith("lieu:")) {
                return TextRole.METADATA;
            }
            return TextRole.BODY;
        }

        private boolean looksLikeContactDetails(String paragraph) {
            String lowerCase = paragraph.toLowerCase(Locale.ROOT);
            return lowerCase.startsWith("phone:")
                    || lowerCase.startsWith("téléphone:")
                    || lowerCase.startsWith("telephone:")
                    || lowerCase.startsWith("email:")
                    || lowerCase.startsWith("e-mail:")
                    || lowerCase.startsWith("location:")
                    || lowerCase.startsWith("lieu:")
                    || lowerCase.startsWith("website:")
                    || lowerCase.startsWith("site:");
        }

        private PDFont fontFor(TextRole role) {
            return switch (role) {
                case SECTION, ENTRY_TITLE -> boldFont;
                case METADATA -> italicFont;
                case BODY, BULLET -> regularFont;
            };
        }

        private float fontSizeFor(TextRole role) {
            return switch (role) {
                case SECTION -> headingSize();
                case ENTRY_TITLE -> bodySize() + 0.8f;
                case METADATA -> bodySize() - 0.2f;
                case BODY, BULLET -> bodySize();
            };
        }

        private float headingSize() {
            return bodyFontSize + (style == ResumePdfStyle.MODERN ? 3.0f : 2.2f);
        }

        private float bodySize() {
            return style == ResumePdfStyle.MINIMAL
                    ? bodyFontSize - 0.2f
                    : bodyFontSize;
        }

        private float findReadableFontSize(List<String> content) throws IOException {
            for (float candidate = DEFAULT_BODY_SIZE;
                 candidate >= MINIMUM_BODY_SIZE;
                 candidate -= 0.25f) {
                if (calculateRequiredHeight(content, candidate) <= availablePageHeight()) {
                    return candidate;
                }
            }

            throw new IllegalArgumentException(
                    "Le contenu est trop long pour tenir sur une page A4 lisible. "
                            + "Raccourcissez le CV avant de générer le PDF."
            );
        }

        private float calculateRequiredHeight(List<String> paragraphs, float candidateBodySize)
                throws IOException {
            float requiredHeight = 0;
            float availableWidth = PDRectangle.A4.getWidth() - (2 * PAGE_MARGIN);

            for (String rawParagraph : paragraphs) {
                String paragraph = sanitize(rawParagraph.strip());
                if (paragraph.isEmpty()) {
                    requiredHeight += 7;
                    continue;
                }

                TextRole role = classify(paragraph);
                boolean heading = role == TextRole.SECTION;
                float baseBodySize = candidateBodySize
                        - (style == ResumePdfStyle.MINIMAL ? 0.2f : 0);
                PDFont font = switch (role) {
                    case SECTION, ENTRY_TITLE -> boldFont;
                    case METADATA -> italicFont;
                    case BODY, BULLET -> regularFont;
                };
                float fontSize = switch (role) {
                    case SECTION -> candidateBodySize
                            + (style == ResumePdfStyle.MODERN ? 3.0f : 2.2f);
                    case ENTRY_TITLE -> baseBodySize + 0.8f;
                    case METADATA -> baseBodySize - 0.2f;
                    case BODY, BULLET -> baseBodySize;
                };
                float lineHeight = fontSize * 1.35f;
                int lineCount = wrap(paragraph, font, fontSize, availableWidth).size();

                requiredHeight += lineCount * lineHeight;
                requiredHeight += heading ? 9 : role == TextRole.ENTRY_TITLE ? 3 : 1;
            }

            return requiredHeight;
        }

        private float availablePageHeight() {
            float bodyStart = style == ResumePdfStyle.PROFESSIONAL ? 716 : 728;
            return bodyStart - BOTTOM_MARGIN;
        }

        private PDColor textColor(TextRole role) {
            if (role == TextRole.METADATA) {
                return new PDColor(new float[]{0.38f, 0.38f, 0.38f}, PDDeviceRGB.INSTANCE);
            }
            if (role != TextRole.SECTION || style == ResumePdfStyle.MINIMAL) {
                return new PDColor(new float[]{0.12f, 0.12f, 0.12f}, PDDeviceRGB.INSTANCE);
            }

            return style == ResumePdfStyle.MODERN
                    ? new PDColor(new float[]{0.02f, 0.38f, 0.45f}, PDDeviceRGB.INSTANCE)
                    : new PDColor(new float[]{0.07f, 0.16f, 0.29f}, PDDeviceRGB.INSTANCE);
        }

        private PDColor white() {
            return new PDColor(new float[]{1, 1, 1}, PDDeviceRGB.INSTANCE);
        }

        private ResumeLayout parseLayout(String content) {
            List<String> lines = new ArrayList<>(List.of(
                    content.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)
            ));
            int contactSectionIndex = findSectionIndex(lines, "contact", "coordonnées");
            int titleSectionIndex = findSectionIndex(
                    lines,
                    "title", "professional title", "headline", "titre", "titre professionnel"
            );
            int titleLabelIndex = titleSectionIndex;
            int firstContentIndex = firstNonBlankIndex(lines, 0);

            int nameIndex = contactSectionIndex >= 0 && contactSectionIndex == firstContentIndex
                    ? firstNonBlankIndex(lines, contactSectionIndex + 1)
                    : firstNonSectionIndex(lines, 0);
            if (nameIndex >= 0 && titleSectionIndex >= 0 && nameIndex == titleSectionIndex) {
                nameIndex = firstNonSectionIndex(lines, titleSectionIndex + 1);
            }
            String name = nameIndex >= 0
                    ? sanitize(lines.get(nameIndex))
                    : "CURRICULUM VITAE";

            int subtitleIndex = titleSectionIndex >= 0
                    ? firstNonBlankIndex(lines, titleSectionIndex + 1)
                    : firstSubtitleIndex(lines, nameIndex + 1);
            String subtitle = subtitleIndex >= 0
                    ? sanitize(lines.get(subtitleIndex))
                    : null;

            if (providedCandidateName != null && !providedCandidateName.isBlank()) {
                int matchingNameIndex = findContentIndex(lines, providedCandidateName);
                if (matchingNameIndex >= 0) {
                    nameIndex = matchingNameIndex;
                }
                name = sanitize(providedCandidateName);
            }
            if (providedProfessionalTitle != null && !providedProfessionalTitle.isBlank()) {
                int matchingTitleIndex = findContentIndex(lines, providedProfessionalTitle);
                if (matchingTitleIndex >= 0) {
                    subtitleIndex = matchingTitleIndex;
                    int previousIndex = previousNonBlankIndex(lines, matchingTitleIndex - 1);
                    if (previousIndex >= 0 && isHeading(sanitize(lines.get(previousIndex)))) {
                        titleLabelIndex = previousIndex;
                    }
                }
                subtitle = sanitize(providedProfessionalTitle);
            }

            List<String> body = new ArrayList<>();
            for (int index = 0; index < lines.size(); index++) {
                if (index != nameIndex
                        && index != subtitleIndex
                        && index != titleLabelIndex) {
                    body.add(lines.get(index));
                }
            }
            while (!body.isEmpty() && body.get(0).isBlank()) {
                body.remove(0);
            }

            return new ResumeLayout(name, subtitle, body);
        }

        private int findContentIndex(List<String> lines, String expectedContent) {
            String sanitizedExpectedContent = sanitize(expectedContent);
            for (int index = 0; index < lines.size(); index++) {
                if (sanitize(lines.get(index)).equalsIgnoreCase(sanitizedExpectedContent)) {
                    return index;
                }
            }
            return -1;
        }

        private int findSectionIndex(List<String> lines, String... labels) {
            Set<String> expectedLabels = Set.of(labels);
            for (int index = 0; index < lines.size(); index++) {
                if (expectedLabels.contains(normalizeLabel(lines.get(index)))) {
                    return index;
                }
            }
            return -1;
        }

        private int firstNonSectionIndex(List<String> lines, int startIndex) {
            for (int index = Math.max(startIndex, 0); index < lines.size(); index++) {
                String line = sanitize(lines.get(index));
                if (!line.isBlank() && !isKnownSection(line)) {
                    return index;
                }
            }
            return -1;
        }

        private int firstSubtitleIndex(List<String> lines, int startIndex) {
            int index = firstNonBlankIndex(lines, startIndex);
            if (index < 0) {
                return -1;
            }
            String candidate = sanitize(lines.get(index));
            return !isHeading(candidate) && candidate.length() <= 90 ? index : -1;
        }

        private int firstNonBlankIndex(List<String> lines, int startIndex) {
            for (int index = Math.max(startIndex, 0); index < lines.size(); index++) {
                if (!lines.get(index).isBlank()) {
                    return index;
                }
            }
            return -1;
        }

        private int previousNonBlankIndex(List<String> lines, int startIndex) {
            for (int index = Math.min(startIndex, lines.size() - 1); index >= 0; index--) {
                if (!lines.get(index).isBlank()) {
                    return index;
                }
            }
            return -1;
        }

        private List<String> wrap(
                String text,
                PDFont font,
                float fontSize,
                float availableWidth
        ) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (String word : text.split("\\s+")) {
                String candidate = currentLine.isEmpty()
                        ? word
                        : currentLine + " " + word;

                if (font.getStringWidth(candidate) / 1000 * fontSize <= availableWidth) {
                    currentLine.setLength(0);
                    currentLine.append(candidate);
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                    }
                    currentLine.setLength(0);
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }

        private String sanitize(String text) {
            String normalizedSymbols = text
                    .replace("\u2194", " <-> ")
                    .replace("\u2192", " -> ")
                    .replace("\u2190", " <- ")
                    .replace("\u21D4", " <=> ")
                    .replace("\u21D2", " => ")
                    .replace("\u21D0", " <= ")
                    .replace("\u2265", " >= ")
                    .replace("\u2264", " <= ")
                    .replace("\u2713", "[x]")
                    .replace("\u2714", "[x]")
                    .replace("\u2717", "[ ]")
                    .replace("\u2718", "[ ]")
                    .replace('\u2022', '-')
                    .replace('\u2013', '-')
                    .replace('\u2014', '-')
                    .replace('\u2018', '\'')
                    .replace('\u2019', '\'')
                    .replace('\u201C', '"')
                    .replace('\u201D', '"')
                    .replace('\u00A0', ' ');

            StringBuilder sanitized = new StringBuilder();
            normalizedSymbols.codePoints().forEach(codePoint ->
                    sanitized.append(toSupportedText(codePoint))
            );
            return sanitized.toString().replaceAll("\\s{2,}", " ").strip();
        }

        private String toSupportedText(int codePoint) {
            String character = new String(Character.toChars(codePoint));
            if (isSupported(character)) {
                return character;
            }

            String asciiFallback = Normalizer.normalize(character, Normalizer.Form.NFKD)
                    .replaceAll("\\p{M}", "")
                    .replaceAll("[^\\x20-\\x7E]", "");

            return !asciiFallback.isEmpty() && isSupported(asciiFallback)
                    ? asciiFallback
                    : "?";
        }

        private boolean isSupported(String text) {
            try {
                regularFont.getStringWidth(text);
                return true;
            } catch (IllegalArgumentException | IOException e) {
                return false;
            }
        }

        private record ResumeLayout(String name, String subtitle, List<String> body) {
        }

        private enum TextRole {
            SECTION,
            ENTRY_TITLE,
            METADATA,
            BULLET,
            BODY
        }
    }
}
