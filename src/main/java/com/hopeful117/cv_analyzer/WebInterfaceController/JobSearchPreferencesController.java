package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.domain.ContractType;
import com.hopeful117.cv_analyzer.search.application.JobSearchPreferencesService;
import com.hopeful117.cv_analyzer.search.application.SearchPreferencesViewModels;
import com.hopeful117.cv_analyzer.search.application.SearchPreferencesViewModels.PreferencesView;
import com.hopeful117.cv_analyzer.search.web.JobSearchPreferencesForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/preferences")
public class JobSearchPreferencesController {

    private final JobSearchPreferencesService preferencesService;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("preferences", preferencesService.getPreferencesView().orElse(null));
        return "preferences";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("preferencesForm")) {
            model.addAttribute("preferencesForm", new JobSearchPreferencesForm());
        }
        prepareForm(model, null);
        return "preferences-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute JobSearchPreferencesForm preferencesForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, null);
            return "preferences-form";
        }
        preferencesService.saveFromForm(preferencesForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Vos préférences de recherche ont été enregistrées.");
        return "redirect:/preferences";
    }

    @GetMapping("/edit")
    public String editForm(Model model) {
        PreferencesView view = preferencesService.getPreferencesView().orElse(null);
        if (view == null) {
            return "redirect:/preferences/new";
        }
        if (!model.containsAttribute("preferencesForm")) {
            model.addAttribute("preferencesForm", SearchPreferencesViewModels.toForm(view));
        }
        prepareForm(model, view.id());
        return "preferences-form";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute JobSearchPreferencesForm preferencesForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, currentId());
            return "preferences-form";
        }
        preferencesService.saveFromForm(preferencesForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Vos préférences de recherche ont été mises à jour.");
        return "redirect:/preferences";
    }

    private Long currentId() {
        return preferencesService.getPreferencesView()
                .map(PreferencesView::id).orElse(null);
    }

    private void prepareForm(Model model, Long preferencesId) {
        model.addAttribute("preferencesId", preferencesId);
        model.addAttribute("contractTypes", ContractType.values());
    }
}
