package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.ProjectionAdministrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings/google-sheets")
public class GoogleSheetsSettingsController {
    private final ProjectionAdministrationService service;

    @GetMapping
    public String settings(Model model) {
        model.addAttribute("settings", service.settings());
        return "google-sheets-settings";
    }

    @PostMapping("/test")
    public String test(RedirectAttributes redirectAttributes) {
        var report = service.testConnection();
        redirectAttributes.addFlashAttribute(report.success() ? "successMessage" : "errorMessage",
                report.message());
        redirectAttributes.addFlashAttribute("connectionReport", report);
        return "redirect:/settings/google-sheets";
    }

    @PostMapping("/retry")
    public String retry(RedirectAttributes redirectAttributes) {
        int count = service.retryFailures();
        redirectAttributes.addFlashAttribute("successMessage",
                count + " projection(s) en erreur ont été relancées.");
        return "redirect:/settings/google-sheets";
    }

    @PostMapping("/rebuild")
    public String rebuild(RedirectAttributes redirectAttributes) {
        var report = service.rebuild();
        redirectAttributes.addFlashAttribute(report.failed() == 0 ? "successMessage" : "errorMessage",
                "Reconstruction : " + report.updated() + " mise(s) à jour, " +
                        report.appended() + " ajout(s), " + report.failed() + " erreur(s).");
        redirectAttributes.addFlashAttribute("rebuildReport", report);
        return "redirect:/settings/google-sheets";
    }
}
