package com.hopeful117.cv_analyzer.config;

import com.hopeful117.cv_analyzer.exception.CvAnalyzerException;
import com.hopeful117.cv_analyzer.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException exception, Model model) {
        model.addAttribute("statusCode", 404);
        model.addAttribute("errorTitle", "Ressource introuvable");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }

    @ExceptionHandler({CvAnalyzerException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(
            RuntimeException exception, HttpServletRequest request, Model model) {
        log.warn("Career Intelligence request rejected on {}: {}", request.getRequestURI(), exception.getMessage());
        model.addAttribute("statusCode", 400);
        model.addAttribute("errorTitle", "Impossible de terminer l’opération");
        model.addAttribute("errorMessage", userMessage(exception));
        model.addAttribute("retryUrl", retryUrl(request.getRequestURI()));
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedError(
            Exception exception, HttpServletRequest request, Model model) {
        log.error("Unexpected Career Intelligence error on {}", request.getRequestURI(), exception);
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorTitle", "Une erreur inattendue est survenue");
        model.addAttribute("errorMessage",
                "L’opération n’a pas pu aboutir. Vos données déjà sauvegardées restent disponibles.");
        model.addAttribute("retryUrl", retryUrl(request.getRequestURI()));
        return "error";
    }

    private String userMessage(RuntimeException exception) {
        if (exception instanceof com.hopeful117.cv_analyzer.exception.AIAnalysisException) {
            return "Le fournisseur IA n’a pas renvoyé une réponse exploitable. Réessayez dans quelques instants.";
        }
        if (exception instanceof com.hopeful117.cv_analyzer.exception.JobScrapperException) {
            return "L’offre n’a pas pu être récupérée depuis cette URL. Collez son texte manuellement.";
        }
        return exception.getMessage();
    }

    private String retryUrl(String path) {
        if (path.startsWith("/generator") || path.startsWith("/cover-letters")) {
            return "/generator";
        }
        if (path.startsWith("/analyze") || path.startsWith("/analyses") || path.startsWith("/resume")) {
            return "/analyze";
        }
        if (path.startsWith("/interview")) {
            return "/interview/start";
        }
        if (path.startsWith("/profile")) {
            return "/profile";
        }
        if (path.startsWith("/applications")) {
            return "/applications";
        }
        if (path.startsWith("/settings/google-sheets")) {
            return "/settings/google-sheets";
        }
        return "/";
    }
}
