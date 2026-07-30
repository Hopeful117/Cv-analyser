package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionService;
import com.hopeful117.cv_analyzer.career.application.consultation.*;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/google-sheets/applications")
public class GoogleSheetsConsultationController {
    private final ConsultGoogleSheetApplicationsUseCase consultationUseCase;
    private final ImportGoogleSheetApplicationUseCase importUseCase;
    private final ApplicationProjectionService projectionService;

    @GetMapping
    public String applications(@RequestParam(required = false) String query,
                               @RequestParam(required = false) String sheetStatus,
                               @RequestParam(required = false) GoogleSheetComparisonState state,
                               @RequestParam(defaultValue = "title") String sort,
                               @RequestParam(defaultValue = "asc") String direction,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        model.addAttribute("consultation",
                consultationUseCase.consult(query, sheetStatus, state, sort, direction, page, 25));
        model.addAttribute("query", query);
        model.addAttribute("sheetStatus", sheetStatus);
        model.addAttribute("selectedState", state);
        model.addAttribute("states", GoogleSheetComparisonState.values());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        return "google-sheets-applications";
    }

    @GetMapping("/rows/{rowNumber}")
    public String legacyDetails(@PathVariable int rowNumber, Model model) {
        model.addAttribute("detail", consultationUseCase.detailsByRowNumber(rowNumber));
        return "google-sheets-application-detail";
    }

    @GetMapping("/rows/{rowNumber}/import")
    public String importPreview(@PathVariable int rowNumber, Model model) {
        var preview = importUseCase.preview(rowNumber);
        model.addAttribute("preview", preview);
        model.addAttribute("applicationForm", preview.form());
        prepareImportForm(model);
        return "google-sheets-application-import";
    }

    @PostMapping("/rows/{rowNumber}/import")
    public String importConfirm(@PathVariable int rowNumber,
                                @Valid @ModelAttribute ApplicationForm applicationForm,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("preview", importUseCase.preview(rowNumber));
            prepareImportForm(model);
            return "google-sheets-application-import";
        }
        long applicationId = importUseCase.importRow(rowNumber, applicationForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "La ligne a été importée dans le CRM et sa synchronisation a été lancée.");
        return "redirect:/applications/" + applicationId;
    }

    @GetMapping("/{careerIntelligenceId}")
    public String details(@PathVariable String careerIntelligenceId, Model model) {
        model.addAttribute("detail", consultationUseCase.details(careerIntelligenceId));
        return "google-sheets-application-detail";
    }

    @PostMapping("/{careerIntelligenceId}/sync")
    public String synchronize(@PathVariable String careerIntelligenceId,
                              RedirectAttributes redirectAttributes) {
        Long applicationId = consultationUseCase.details(careerIntelligenceId)
                .comparison().crmApplicationId();
        if (applicationId == null) {
            throw new IllegalArgumentException(
                    "Cette ligne Google Sheets n’est liée à aucune candidature du CRM.");
        }
        projectionService.synchronize(applicationId);
        redirectAttributes.addFlashAttribute("successMessage",
                "La candidature a été resynchronisée depuis Career Intelligence.");
        return "redirect:/google-sheets/applications/" + careerIntelligenceId;
    }

    private void prepareImportForm(Model model) {
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("priorities", ApplicationPriority.values());
        model.addAttribute("remoteModes", RemoteMode.values());
    }
}
