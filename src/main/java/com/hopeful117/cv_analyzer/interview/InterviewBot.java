package com.hopeful117.cv_analyzer.interview;

import com.hopeful117.cv_analyzer.dto.InterviewQuestionDto;
import com.hopeful117.cv_analyzer.exception.AIAnalysisException;
import com.hopeful117.cv_analyzer.model.InterviewQuestion;
import com.hopeful117.cv_analyzer.dto.InterviewQuestionFeedback;
import com.hopeful117.cv_analyzer.model.InterviewQuestionResult;
import com.hopeful117.cv_analyzer.model.InterviewReport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterviewBot {
    private final ChatClient chatClient;

    public InterviewBot(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public List<InterviewQuestionDto> generateQuestions(String cv, String jobDescription) {
        try {
            return chatClient.prompt()
                    .system("""
                              Tu es un recruteur senior spécialisé dans les métiers de l'informatique,
                              tu analyses les cv de manières objectives et tu aides les gens à se preparer pour leur entretien d'embauche.
                            
                                Tu renvois exclusivement de JSON valide:
                                {
                                  "questions": [
                                    {
                                      "order": 1,
                                      "category": "...",
                                      "question": "...",
                                      "expectedSkill": "..."
                                    }
                                  ]
                                }
                            """)
                    .user(u -> u.text("""
                                CV:
                                {cv}
                                
                                Job Description:
                                {jobDescription}
                            """)
                            .param("cv", cv)
                            .param("jobDescription", jobDescription))
                    .call()
                          .entity(new ParameterizedTypeReference<List<InterviewQuestionDto>>() {
                          });
        } catch (Exception e) {
            throw new AIAnalysisException("Error generating interview questions", e);
        }
    }
    public InterviewQuestionFeedback generateFeedback(InterviewQuestion question, String answer) {
        try {
            return chatClient.prompt()
                    .system("""
                              Tu es un recruteur senior spécialisé dans les métiers de l'informatique,
                              tu analyses les cv de manières objectives et tu aides les gens à se preparer pour leur entretien d'embauche.
                            
                                Tu renvois exclusivement de JSON valide:
                                {
                                    "technicalAccuracy": 0,
                                    "clarity": 0,
                                    "confidenceLevel": 0,
                                    "relevance": 0,
                                    "strengths": ["..."],
                                    "improvements": ["..."],
                                    "suggestedAnswer": "..."
                                }
                            """)
                    .user(u -> u.text("""
                            
                           
                                Question:
                                {question}
                       
                                Answer:
                                {answer}
                            """)

                            .param("question", question)
                            .param("answer", answer))
                    .call()
                    .entity(InterviewQuestionFeedback.class);
        } catch (Exception e) {
            throw new AIAnalysisException("Error generating interview feedback", e);
        }
    }
    public InterviewReport generateReport (List<InterviewQuestionResult> results) {
        try {
            return chatClient.prompt()
                    .system("""
                              Tu es un recruteur senior spécialisé dans les métiers de l'informatique,
                              tu analyses les cv de manières objectives et tu aides les gens à se preparer pour leur entretien d'embauche.
                            
                                Tu renvois exclusivement de JSON valide:
                                {
                                    "overallScore": 0,
                                    "strengths": ["..."],
                                    "improvements": ["..."],
                                    "recommendation": "..."
                                }
                            """)
                    .user(u -> u.text("""
                            
                           
                                Results:
                                {results}
                       
                            """)
                            .param("results", results))
                    .call()
                    .entity(InterviewReport.class);
        } catch (Exception e) {
            throw new AIAnalysisException("Error generating interview report", e);
        }
    }
}


