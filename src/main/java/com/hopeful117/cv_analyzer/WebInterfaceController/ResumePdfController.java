package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import com.hopeful117.cv_analyzer.service.ResumePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class ResumePdfController {
    private final ResumePdfService resumePdfService;

    @PostMapping("/resume/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam String resumeContent,
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String professionalTitle,
            @RequestParam(defaultValue = "PROFESSIONAL") ResumePdfStyle style
    ) throws IOException {
        byte[] pdf = resumePdfService.generate(
                resumeContent,
                style,
                candidateName,
                professionalTitle
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("cv-corrige.pdf", StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidResumeContent(IllegalArgumentException exception) {
        return ResponseEntity.unprocessableContent()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }
}
