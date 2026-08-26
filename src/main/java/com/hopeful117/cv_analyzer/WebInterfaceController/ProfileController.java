package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.profile.application.ProfessionalProfileService;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProfileView;
import com.hopeful117.cv_analyzer.profile.application.ProfileViewModels.ProposalReview;
import com.hopeful117.cv_analyzer.profile.web.ProfileForm;
import com.hopeful117.cv_analyzer.profile.web.ProfileProposalForm;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfessionalProfileService profileService;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("profile", profileService.getProfileView().orElse(null));
        return "profile";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", emptyManualForm());
        }
        model.addAttribute("profileId", null);
        return "profile-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ProfileForm profileForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profileId", null);
            return "profile-form";
        }
        profileService.saveFromForm(profileForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Votre profil professionnel a été créé.");
        return "redirect:/profile";
    }

    @GetMapping("/edit")
    public String editForm(Model model) {
        ProfileView view = profileService.getProfileView().orElse(null);
        if (view == null) {
            return "redirect:/profile/new";
        }
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", toForm(view));
        }
        model.addAttribute("profileId", view.id());
        return "profile-form";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute ProfileForm profileForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profileId", profileService.getProfileView()
                    .map(ProfileView::id).orElse(null));
            return "profile-form";
        }
        profileService.saveFromForm(profileForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Votre profil professionnel a été mis à jour.");
        return "redirect:/profile";
    }

    @GetMapping("/initialize")
    public String initializeForm() {
        return "profile-initialize";
    }

    /**
     * Extraction IA : rend directement l'écran de revue (précédent : résultat d'analyse rendu
     * depuis le POST /analyze). Aucune écriture : le profil fiable reste intact tant que la
     * proposition n'est pas validée.
     */
    @PostMapping("/initialize")
    public String initialize(@RequestParam("cvFile") MultipartFile cvFile, Model model) {
        ProposalReview review = profileService.proposeFromCv(cvFile);
        model.addAttribute("proposalForm", review.form());
        model.addAttribute("aiProvider", review.aiProvider());
        model.addAttribute("aiModel", review.aiModel());
        return "profile-proposal";
    }

    @PostMapping("/apply")
    public String applyProposal(@Valid @ModelAttribute("proposalForm") ProfileProposalForm proposalForm,
                                BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("aiProvider", proposalForm.getAiProvider());
            model.addAttribute("aiModel", proposalForm.getAiModel());
            return "profile-proposal";
        }
        profileService.applyProposal(proposalForm);
        redirectAttributes.addFlashAttribute("successMessage",
                "Les données validées ont été ajoutées à votre profil professionnel.");
        return "redirect:/profile";
    }

    private ProfileForm toForm(ProfileView view) {
        return ProfileViewModels.toManualForm(view);
    }

    private ProfileForm emptyManualForm() {
        return ProfileViewModels.emptyManualForm();
    }
}
