package com.kareem.GitMatch.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for Gemini API calls.
 */
public record GeminiRequest(List<Content> contents) {

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    /**
     * Creates a simple text prompt request for Gemini.
     */
    public static GeminiRequest fromPrompt(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt))))
        );
    }
}
