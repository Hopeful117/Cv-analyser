package com.hopeful117.cv_analyzer.WebInterfaceController;

import com.hopeful117.cv_analyzer.model.InterviewQuestion;
import com.hopeful117.cv_analyzer.model.InterviewReport;
import com.hopeful117.cv_analyzer.model.InterviewSession;
import com.hopeful117.cv_analyzer.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/interview")
@Slf4j
public class InterviewController {
    private final InterviewService interviewService;


    @GetMapping("/start")
    public String startForm() {
        return "start";
    }

    @PostMapping("/start")
    public String startInterview(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String jobOffer,
            @RequestParam(required = false) String jobOfferUrl) throws Exception {


        InterviewSession session =
                interviewService.createSession(file,jobOffer,jobOfferUrl);

        return "redirect:/interview/" + session.getSessionId();
    }

    @GetMapping("/{sessionId}")
    public String interviewPage(@PathVariable("sessionId") UUID sessionId,
                                Model model) {

        InterviewSession session = interviewService.getSession(sessionId);

        InterviewQuestion question =
                interviewService.getCurrentQuestion(sessionId);

        model.addAttribute("id", session.getSessionId());
        model.addAttribute("question", question);


        return "chat";
    }

    @PostMapping("/{sessionId}/answer")
    public String submitAnswer(@PathVariable UUID sessionId,
                               @RequestParam String answer) {

        interviewService.submitAnswer(sessionId, answer);

        InterviewSession session = interviewService.getSession(sessionId);

        if (session.getCurrentIndex() >= session.getQuestions().size()) {
            return "redirect:/interview/" + sessionId + "/report";
        }

        return "redirect:/interview/" + sessionId;
    }
    @GetMapping("/{sessionId}/report")
    public String report(@PathVariable UUID sessionId,
                         Model model) {

        InterviewReport report =
                interviewService.generateReport(sessionId);

        model.addAttribute("report", report);

        return "report";
    }
}
