package com.hopeful117.cv_analyzer.discovery.infrastructure.francetravail;

public class FranceTravailAuthenticationException extends RuntimeException {
    public FranceTravailAuthenticationException(String message) {
        super(message);
    }

    public FranceTravailAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
