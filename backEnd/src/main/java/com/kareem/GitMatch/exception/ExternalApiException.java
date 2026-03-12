package com.kareem.GitMatch.exception;

/**
 * Thrown when an external API call (GitHub, Gemini, etc.) fails.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
