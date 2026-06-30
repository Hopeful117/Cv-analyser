package com.hopeful117.cv_analyzer.config;

import com.hopeful117.cv_analyzer.exception.CvAnalyzerException;
import com.hopeful117.cv_analyzer.exception.InvalidJobOfferException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
/*
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(
            CvAnalyzerException.class
    )
    public String handleBusinessException(
            CvAnalyzerException ex,
            Model model) {

        model.addAttribute(
                "errorMessage",
                ex.getMessage()
        );

        return "home";
    }
    @ExceptionHandler(Exception.class)
    public String handleUnexpectedError(
            Exception ex,
            Model model) {

        model.addAttribute(
                "errorMessage",
                "Une erreur inattendue est survenue."
        );

        return "home";
    }
}
*/