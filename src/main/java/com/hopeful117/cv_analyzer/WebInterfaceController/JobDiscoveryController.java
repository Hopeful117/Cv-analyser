package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.discovery.application.DiscoverJobOffers;
import com.hopeful117.cv_analyzer.discovery.application.EvaluateJobOffer;
import com.hopeful117.cv_analyzer.discovery.application.SelectJobOffer;
import com.hopeful117.cv_analyzer.discovery.web.JobDiscoveryViewModels;
import com.hopeful117.cv_analyzer.discovery.web.ManualJobOfferForm;
import com.hopeful117.cv_analyzer.discovery.web.JobOfferSelectionForm;
import com.hopeful117.cv_analyzer.discovery.infrastructure.greenhouse.GreenhouseBoardService;
import com.hopeful117.cv_analyzer.search.domain.WorkMode;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/job-discovery")
@RequiredArgsConstructor
public class JobDiscoveryController {

    private final DiscoverJobOffers discoverJobOffers;
    private final SelectJobOffer selectJobOffer;
    private final EvaluateJobOffer evaluateJobOffer;
    private final JobSearchPreferencesRepository preferencesRepository;
    private final GreenhouseBoardService greenhouseBoardService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("greenhouseBoards", greenhouseBoardService.configuredBoards());
        var preferences = preferencesRepository.findActivePreferences();
        if (preferences.isEmpty()) {
            model.addAttribute("errorMessage", "Vos préférences de recherche ne sont pas encore configurées.");
            return "job-discovery";
        }

        var roles = preferences.get().getTargetRoles();
        if (roles == null || roles.isEmpty()) {
            model.addAttribute("errorMessage", "Aucun rôle cible n'est configuré dans vos préférences.");
            return "job-discovery";
        }

        model.addAttribute("searchForm", JobDiscoveryViewModels.createSearchForm(roles));
        return "job-discovery";
    }

    @PostMapping("/search")
    public String search(@RequestParam String targetRole, Model model, RedirectAttributes redirectAttributes) {
        if (targetRole == null || targetRole.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner un rôle.");
            return "redirect:/job-discovery";
        }

        DiscoverJobOffers.DiscoveryResult result = discoverJobOffers.discover(targetRole);

        if (!result.success()) {
            model.addAttribute("errorMessage", result.errorMessage());
            var preferences = preferencesRepository.findActivePreferences();
            if (preferences.isPresent() && preferences.get().getTargetRoles() != null) {
                model.addAttribute("searchForm", JobDiscoveryViewModels.createSearchForm(preferences.get().getTargetRoles()));
            }
            model.addAttribute("greenhouseBoards", greenhouseBoardService.configuredBoards());
            return "job-discovery";
        }

        model.addAttribute("results", JobDiscoveryViewModels.toResults(result));
        model.addAttribute("selectedRole", targetRole);
        return "job-discovery-results";
    }

    @GetMapping("/manual")
    public String manualForm(Model model) {
        addManualOptions(model);
        if (!model.containsAttribute("manualForm")) {
            model.addAttribute("manualForm", new ManualJobOfferForm());
        }
        return "job-discovery-manual";
    }

    @PostMapping("/manual/preview")
    public String manualPreview(@Valid @ModelAttribute("manualForm") ManualJobOfferForm form,
                                BindingResult bindingResult, Model model) {
        addManualOptions(model);
        if (bindingResult.hasErrors()) {
            return "job-discovery-manual";
        }

        var offer = form.toJobOffer();
        var evaluation = evaluateJobOffer.evaluate(offer);
        if (!evaluation.success()) {
            model.addAttribute("errorMessage", evaluation.errorMessage());
            return "job-discovery-manual";
        }

        model.addAttribute("manualForm", form);
        model.addAttribute("preview", JobDiscoveryViewModels.toOfferViewModel(
                offer, evaluation.eligibility(), evaluation.matching()));
        return "job-discovery-manual-preview";
    }

    @PostMapping("/manual/select")
    public String selectManual(@Valid @ModelAttribute("manualForm") ManualJobOfferForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "L’offre manuelle est invalide.");
            return "redirect:/job-discovery/manual";
        }
        Long opportunityId = selectJobOffer.select(form.toJobOffer());
        redirectAttributes.addFlashAttribute("successMessage", "L’opportunité a été ajoutée.");
        return "redirect:/opportunities/" + opportunityId;
    }

    private static void addManualOptions(Model model) {
        model.addAttribute("workModes", WorkMode.values());
        model.addAttribute("contractTypes", com.hopeful117.cv_analyzer.career.domain.ContractType.values());
    }

    @PostMapping("/select")
    public String select(@Valid JobOfferSelectionForm form, BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        return selectOffer(form, bindingResult, redirectAttributes, "redirect:/job-discovery");
    }

    @GetMapping("/greenhouse")
    public String greenhouse(Model model) {
        model.addAttribute("greenhouseBoards", greenhouseBoardService.configuredBoards());
        return "job-discovery-greenhouse";
    }

    @PostMapping("/greenhouse/search")
    public String greenhouseSearch(Model model) {
        String readinessError = evaluateJobOffer.readinessError();
        if (readinessError != null) {
            model.addAttribute("errorMessage", readinessError);
            model.addAttribute("greenhouseBoards", greenhouseBoardService.configuredBoards());
            return "job-discovery-greenhouse";
        }

        GreenhouseBoardService.BoardFetchResult fetched = greenhouseBoardService.fetchAll();
        var offers = fetched.offers().stream()
                .map(offer -> {
                    var evaluation = evaluateJobOffer.evaluate(offer);
                    return evaluation.success()
                            ? new DiscoverJobOffers.EligibleOffer(offer, evaluation.eligibility(), evaluation.matching())
                            : null;
                })
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt((DiscoverJobOffers.EligibleOffer item) -> item.matching().score()).reversed())
                .toList();

        model.addAttribute("results", JobDiscoveryViewModels.toResults(
                DiscoverJobOffers.DiscoveryResult.success(
                        offers, offers.size(), offers.size(), "Boards Greenhouse configurés", "greenhouse")));
        model.addAttribute("boardErrors", fetched.errors());
        return "job-discovery-greenhouse-results";
    }

    @PostMapping("/greenhouse/select")
    public String selectGreenhouse(@Valid JobOfferSelectionForm form, BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        return selectOffer(form, bindingResult, redirectAttributes, "redirect:/job-discovery/greenhouse");
    }

    private String selectOffer(JobOfferSelectionForm form, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, String errorRedirect) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "L’offre sélectionnée est invalide.");
            return errorRedirect;
        }
        Long opportunityId = selectJobOffer.select(form.toJobOffer());
        redirectAttributes.addFlashAttribute("successMessage", "L’opportunité a été ajoutée.");
        return "redirect:/opportunities/" + opportunityId;
    }
}
