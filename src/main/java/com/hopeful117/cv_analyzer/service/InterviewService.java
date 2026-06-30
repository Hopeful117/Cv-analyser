package com.hopeful117.cv_analyzer.service;

import com.hopeful117.cv_analyzer.dto.InterviewQuestionDto;
import com.hopeful117.cv_analyzer.dto.InterviewQuestionFeedback;
import com.hopeful117.cv_analyzer.helper.ResolveOffer;
import com.hopeful117.cv_analyzer.interview.InterviewBot;
import com.hopeful117.cv_analyzer.model.*;
import com.hopeful117.cv_analyzer.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {
    private final InterviewBot interviewBot;
    private final ResolveOffer resolveOffer;
    private final PdfParserService pdfParserService;
    private final SessionRepository sessionRepository;

    public InterviewSession createSession(MultipartFile cvFile, String jobOfferText, String jobOfferUrl) throws Exception {
        String jobDescription = resolveOffer.resolveOffer(jobOfferText, jobOfferUrl);
        String cv = pdfParserService.extractText(cvFile);
        InterviewSession session = new InterviewSession();
        session.setCv(cv);
        session.setJobDescription(jobDescription);

       List<InterviewQuestionDto> questions =
                interviewBot.generateQuestions(cv, jobDescription);

        session.setQuestions(mapToEntity(questions, session));
        session.setCurrentIndex(0);

        sessionRepository.save(session);


        return session;
    }

    public InterviewQuestion nextQuestion(UUID sessionId) {

        InterviewSession session = sessionRepository.findBySessionId(sessionId).orElse(null);

        assert session != null;
        return session.getQuestions().get(session.getCurrentIndex());
    }
    public void submitAnswer(UUID sessionId, String answer) {

        InterviewSession session = sessionRepository.findBySessionId(sessionId).orElse(null);

        assert session != null;
        InterviewQuestion question =
                session.getQuestions().get(session.getCurrentIndex());

        InterviewQuestionFeedback feedback =
                interviewBot.generateFeedback(question, answer);
        session.getResults().add(new InterviewQuestionResult( session, question, answer,feedback));

        session.setCurrentIndex(session.getCurrentIndex() + 1);
        sessionRepository.save(session);
    }
    public InterviewReport generateReport(UUID sessionId) {

        InterviewSession session = sessionRepository.findBySessionId(sessionId).orElse(null);

        assert session != null;
        InterviewReport report = interviewBot.generateReport(session.getResults());
        sessionRepository.save(session);
        return report;
    }

    public InterviewSession getSession(UUID id) {
        return sessionRepository.findBySessionId(id).orElse(null);
    }

    public InterviewQuestion getCurrentQuestion(UUID id) {
        InterviewSession session = sessionRepository.findBySessionId(id).orElse(null);
        assert session != null;
        return session.getQuestions().get(session.getCurrentIndex());
    }

    private List<InterviewQuestion>mapToEntity(List<InterviewQuestionDto> questionDtos, InterviewSession session) {
        return questionDtos.stream()
                .map(dto -> new InterviewQuestion(session, dto.getOrder(), dto.getCategory(), dto.getQuestion(), dto.getExpectedSkill()))
                .toList();
    }


}
