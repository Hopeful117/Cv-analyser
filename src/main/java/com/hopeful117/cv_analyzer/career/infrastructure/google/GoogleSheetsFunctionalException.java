package com.hopeful117.cv_analyzer.career.infrastructure.google;

public class GoogleSheetsFunctionalException extends RuntimeException {
    private final String code;

    public GoogleSheetsFunctionalException(String code, String message) {
        super(message);
        this.code = code;
    }

    public GoogleSheetsFunctionalException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
