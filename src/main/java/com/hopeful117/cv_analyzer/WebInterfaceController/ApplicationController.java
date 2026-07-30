package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import com.hopeful117.cv_analyzer.career.application.ApplicationProjectionService;
import com.hopeful117.cv_analyzer.career.domain.*;
import com.hopeful117.cv_analyzer.career.web.ApplicationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {
    private final ApplicationCrmService crmService;
    private final ApplicationProjectionService projectionService;

    @GetMapping
    public String list(@RequestParam(required = false) String query,
                       @RequestParam(required = false) ApplicationStatus status,
                       @RequestParam(required = false) ApplicationPriority priority,
                       @RequestParam(defaultValue = "false") boolean followUpsDue,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "desc") String direction,
                       Model model) {
        model.addAttribute("applicationsPage",
                crmService.search(query, status, priority, followUpsDue, page, 20, direction));
        model.addAttribute("query", query);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("followUpsDue", followUpsDue);
        model.addAttribute("direction", direction);
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("priorities", ApplicationPriority.values());
        return "applications";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("applicationForm")) {
            model.addAttribute("applicationForm", new ApplicationForm());
        }
        prepareForm(model, null);
        return "application-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ApplicationForm applicationForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, null);
            return "application-form";
        }
        Long id = crmService.create(applicationForm, ChangeSource.USER);
        redirectAttributes.addFlashAttribute("successMessage", "La candidature a été enregistrée.");
        return "redirect:/applications/" + id;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, Model model) {
        model.addAttribute("application", crmService.getDetails(id));
        model.addAttribute("statuses", ApplicationStatus.values());
        return "application-detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        if (!model.containsAttribute("applicationForm")) {
            model.addAttribute("applicationForm", crmService.getForm(id));
        }
        prepareForm(model, id);
        return "application-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id,
                         @Valid @ModelAttribute ApplicationForm applicationForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, id);
            return "application-form";
        }
        crmService.update(id, applicationForm);
        redirectAttributes.addFlashAttribute("successMessage", "La candidature a été mise à jour.");
        return "redirect:/applications/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable long id, @RequestParam ApplicationStatus status,
                               @RequestParam(required = false) String comment,
                               RedirectAttributes redirectAttributes) {
        crmService.changeStatus(id, status, comment);
        redirectAttributes.addFlashAttribute("successMessage", "Le statut a été mis à jour.");
        return "redirect:/applications/" + id;
    }

    @PostMapping("/{id}/follow-ups")
    public String followUp(@PathVariable long id,
                           @RequestParam(defaultValue = "plan") String action,
                           @RequestParam(required = false) LocalDate date,
                           @RequestParam(required = false) String comment,
                           RedirectAttributes redirectAttributes) {
        if ("record".equals(action)) {
            crmService.recordFollowUp(id, date, comment);
            redirectAttributes.addFlashAttribute("successMessage", "La relance a été enregistrée.");
        } else {
            crmService.planFollowUp(id, date);
            redirectAttributes.addFlashAttribute("successMessage", "La relance a été planifiée.");
        }
        return "redirect:/applications/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        crmService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "La candidature a été supprimée.");
        return "redirect:/applications";
    }

    @PostMapping("/{id}/google-sheets/sync")
    public String synchronize(@PathVariable long id, RedirectAttributes redirectAttributes) {
        projectionService.synchronize(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "La tentative de synchronisation est terminée. Consultez son état ci-dessous.");
        return "redirect:/applications/" + id;
    }

    private void prepareForm(Model model, Long id) {
        model.addAttribute("applicationId", id);
        model.addAttribute("options", crmService.getFormOptions());
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("priorities", ApplicationPriority.values());
        model.addAttribute("contractTypes", ContractType.values());
        model.addAttribute("workSchedules", WorkSchedule.values());
        model.addAttribute("remoteModes", RemoteMode.values());
        model.addAttribute("interviewStatuses", InterviewStatus.values());
        model.addAttribute("decisions", ApplicationDecision.values());
    }
}
