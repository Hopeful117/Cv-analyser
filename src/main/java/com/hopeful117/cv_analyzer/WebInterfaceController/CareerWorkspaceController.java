package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService;
import com.hopeful117.cv_analyzer.career.application.CareerViewModels.AnalysisDetails;
import com.hopeful117.cv_analyzer.model.ResumePdfStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CareerWorkspaceController {
    private final CareerWorkspaceService careerWorkspaceService;

    @GetMapping("/analyses")
    public String analyses(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("analysesPage", careerWorkspaceService.getAnalyses(page, 10));
        return "analyses";
    }

    @GetMapping("/analyses/{id}")
    public String analysis(@PathVariable long id, Model model) {
        AnalysisDetails details = careerWorkspaceService.getAnalysis(id);
        model.addAttribute("details", details);
        model.addAttribute("analysis", details.analysis());
        model.addAttribute("generatedResume", details.generatedResume());
        return "result-analyzer";
    }

    @PostMapping("/resumes/{documentId}/versions")
    public String saveResumeVersion(@PathVariable long documentId,
                                    @RequestParam String resumeContent,
                                    @RequestParam(required = false) String candidateName,
                                    @RequestParam(required = false) String professionalTitle,
                                    @RequestParam(defaultValue = "PROFESSIONAL") ResumePdfStyle style,
                                    RedirectAttributes redirectAttributes) {
        Long analysisId = careerWorkspaceService.saveResumeVersion(
                documentId, resumeContent, candidateName, professionalTitle, style);
        redirectAttributes.addFlashAttribute("successMessage", "Une nouvelle version du CV a été enregistrée.");
        return "redirect:/analyses/" + analysisId;
    }

    @GetMapping("/cover-letters/{id}")
    public String coverLetter(@PathVariable long id, Model model) {
        var letter = careerWorkspaceService.getCoverLetter(id);
        model.addAttribute("letter", letter);
        model.addAttribute("coverLetter", letter.content());
        return "result-generator";
    }

    @PostMapping("/cover-letters/{id}")
    public String updateCoverLetter(@PathVariable long id, @RequestParam String content,
                                    RedirectAttributes redirectAttributes) {
        careerWorkspaceService.updateCoverLetter(id, content);
        redirectAttributes.addFlashAttribute("successMessage", "La lettre a été enregistrée.");
        return "redirect:/cover-letters/" + id;
    }

    @PostMapping("/analyses/{id}/delete")
    public String deleteAnalysis(@PathVariable long id, RedirectAttributes redirectAttributes) {
        careerWorkspaceService.deleteAnalysis(id);
        redirectAttributes.addFlashAttribute("successMessage", "L’analyse et son CV ont été supprimés.");
        return "redirect:/analyses";
    }

    @PostMapping("/resumes/{id}/delete")
    public String deleteResume(@PathVariable long id, RedirectAttributes redirectAttributes) {
        careerWorkspaceService.deleteResume(id);
        redirectAttributes.addFlashAttribute("successMessage", "Le CV et ses versions ont été supprimés.");
        return "redirect:/";
    }

    @PostMapping("/cover-letters/{id}/delete")
    public String deleteCoverLetter(@PathVariable long id, RedirectAttributes redirectAttributes) {
        careerWorkspaceService.deleteCoverLetter(id);
        redirectAttributes.addFlashAttribute("successMessage", "La lettre a été supprimée.");
        return "redirect:/";
    }

    @PostMapping("/opportunities/{id}/delete")
    public String deleteOpportunity(@PathVariable long id, RedirectAttributes redirectAttributes) {
        careerWorkspaceService.deleteOpportunity(id);
        redirectAttributes.addFlashAttribute("successMessage", "L’opportunité a été supprimée.");
        return "redirect:/";
    }
}
