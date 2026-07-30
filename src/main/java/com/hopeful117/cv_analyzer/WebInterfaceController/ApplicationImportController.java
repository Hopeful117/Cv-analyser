package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.importer.ExcelApplicationImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/applications/import")
public class ApplicationImportController {
    private final ExcelApplicationImportService importService;

    @GetMapping
    public String form() {
        return "application-import";
    }

    @PostMapping("/preview")
    public String preview(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("preview", importService.preview(file));
        return "application-import";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam String token, RedirectAttributes redirectAttributes) {
        var report = importService.confirm(token);
        redirectAttributes.addFlashAttribute(report.errors().isEmpty() ? "successMessage" : "errorMessage",
                report.imported() + " candidature(s) importée(s), " + report.skipped() + " ignorée(s).");
        return "redirect:/applications";
    }
}
