package com.hopeful117.cv_analyzer.exception;

public abstract class CvAnalyzerException extends RuntimeException {
    protected CvAnalyzerException(
            String message) {

        super(message);
    }

    protected CvAnalyzerException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
