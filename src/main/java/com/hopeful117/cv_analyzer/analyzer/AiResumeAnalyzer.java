package com.hopeful117.cv_analyzer.analyzer;

import com.hopeful117.cv_analyzer.exception.AIAnalysisException;
import com.hopeful117.cv_analyzer.model.ResumeAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import javax.xml.transform.Result;


@Component
public class AiResumeAnalyzer implements Analyzer {
    private final ChatClient chatClient;

    public AiResumeAnalyzer(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public ResumeAnalysis analyze(String resumeText, String jobOfferText) {
        try {

            return chatClient.prompt()
                    .system("""
                            Tu es un expert ATS et recruteur senior spécialisé dans les métiers de l'informatique.
                            
                            Tu analyses des CV de manière objective.
                            
                            Tu réponds toujours conformément au schéma Java fourni par l'application.
                            
                            Tu ne produis jamais de texte libre, d'introduction, de conclusion ou de Markdown.
                            
                            Lorsque certaines informations sont absentes, tu retournes simplement une liste vide ou une valeur cohérente.
                            
                            Les scores sont toujours compris entre 0 et 100.
                            """).user(u -> u.text("""
                                    Analyse ce CV par rapport à cette offre d'emploi.
                                    
                                    CV :
                                    {cv}
                                    
                                    Offre :
                                    {job}
                                    """)
                            .param("cv", resumeText)
                            .param("job", jobOfferText))
                    .call()
                    .entity(ResumeAnalysis.class);
        } catch (Exception e) {
            throw new AIAnalysisException("Erreur lors de l'analyse du CV avec l'IA : " + e.getMessage(), e);

        }
    }
}
