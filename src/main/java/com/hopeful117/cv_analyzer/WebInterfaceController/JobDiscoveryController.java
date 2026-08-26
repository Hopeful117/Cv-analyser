package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.discovery.application.DiscoverJobOffers;
import com.hopeful117.cv_analyzer.discovery.web.JobDiscoveryViewModels;
import com.hopeful117.cv_analyzer.search.persistence.JobSearchPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/job-discovery")
@RequiredArgsConstructor
public class JobDiscoveryController {

    private final DiscoverJobOffers discoverJobOffers;
    private final JobSearchPreferencesRepository preferencesRepository;

    @GetMapping
    public String index(Model model) {
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
            return "job-discovery";
        }

        model.addAttribute("results", JobDiscoveryViewModels.toResults(result));
        model.addAttribute("selectedRole", targetRole);
        return "job-discovery-results";
    }
}
