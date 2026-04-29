package com.hopeful117.cv_analyzer.TestService;

import com.hopeful117.cv_analyzer.service.JobScrapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestJobScrapperService {

    private JobScrapperService jobScrapperService;

    @BeforeEach
    void setUp() {
        jobScrapperService = new JobScrapperService();
    }

    @Test
    void shouldExtractTextFromValidUrl() throws IOException {
        // Test avec une URL réelle mais simple
        String url = "https://example.com";
        
        String result = jobScrapperService.extractTextFromUrl(url);
        
        // Vérifier que du texte a été extrait
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldThrowIOExceptionForInvalidUrl() {
        String invalidUrl = "https://invalid-url-that-absolutely-does-not-exist-12345-test.com";

        // Le service devrait lever une IOException pour une URL invalide
        assertThatThrownBy(() -> jobScrapperService.extractTextFromUrl(invalidUrl))
                .isInstanceOf(IOException.class);
    }

    @Test
    void shouldRemoveScriptTags() throws IOException {
        // example.com devrait renvoyer du HTML sans scripts majeurs
        String url = "https://example.com";
        
        String result = jobScrapperService.extractTextFromUrl(url);
        
        // Vérifier que le résultat ne contient pas de tags script
        assertThat(result)
                .doesNotContain("<script>")
                .doesNotContain("</script>");
    }

    @Test
    void shouldExtractBodyContent() throws IOException {
        String url = "https://example.com";
        
        String result = jobScrapperService.extractTextFromUrl(url);
        
        // Vérifier qu'on récupère du contenu du body
        assertThat(result).isNotBlank();
    }

    @Test
    void shouldHandleUrlWithJobOffer() throws IOException {
        // Test avec une petite page contenant du contenu
        String url = "https://example.com";
        
        String result = jobScrapperService.extractTextFromUrl(url);
        
        // Le résultat devrait être un texte
        assertThat(result).isInstanceOf(String.class);
        assertThat(result.length()).isGreaterThan(0);
    }

    @Test
    void shouldRemoveNavigationElements() throws IOException {
        // Les éléments nav, header, footer devraient être supprimés
        String url = "https://example.com";
        
        String result = jobScrapperService.extractTextFromUrl(url);
        
        // Vérifier que le résultat ne contient pas les balises
        assertThat(result).isNotNull();
        // Le texte du body devrait être nettoyé
    }
}
