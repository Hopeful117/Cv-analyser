package com.hopeful117.cv_analyzer.generator;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class AiCoverLetterGenerator {
    private final ChatClient chatClient;
    public AiCoverLetterGenerator(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    public String  generateCoverLetter(String lettre,String cv,String jobOfferText) {

        return chatClient.prompt()
                .system("""
                        Tu es un expert RH et recruteur senior spécialisé dans les métiers de la tech.
                        
                        Tu aides les candidats à rédiger des lettres de motivation professionnelles, impactantes et adaptées aux offres d’emploi.
                        
                        Tu respectes toujours un ton professionnel, clair et naturel.
                        Tu évites les phrases génériques ou trop marketing.
                        Tu adaptes toujours la lettre à l’offre et au CV fournis.
                        Si la lettre est vide tu en génère une de toute pièces, sinon tu adaptes et tu améliores celle existante
                        """).user(u -> u.text("""
                                Rédige une lettre de motivation pour ce CV par rapport à cette offre d'emploi.
                                Lettre:
                                {lettre}
                                
                                CV :
                                {cv}
                                
                                Offre :
                                {job}
                                """)
                        .param("cv", cv)
                        .param("job", jobOfferText)
                        .param("lettre", lettre)
                )
                .call()
                .entity(String.class);
    }
}
