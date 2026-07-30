package com.hopeful117.cv_analyzer.WebInterfaceController;

import lombok.RequiredArgsConstructor;
import com.hopeful117.cv_analyzer.career.application.CareerWorkspaceService;
import com.hopeful117.cv_analyzer.career.application.ApplicationCrmService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final CareerWorkspaceService careerWorkspaceService;
    private final ApplicationCrmService applicationCrmService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("dashboard", careerWorkspaceService.getDashboard());
        model.addAttribute("crmDashboard", applicationCrmService.dashboard());
        return "home";
    }


}
