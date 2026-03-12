package com.kareem.GitMatch.ai.dto;

import java.util.List;

/**
 * Response DTO for Gemini API calls.
 */
public record GeminiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    /**
     * Extracts the text from the first candidate's first part.
     *
     * @return the generated text, or null if no content
     */
    public String extractText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate candidate = candidates.getFirst();
            if (candidate.content() != null && candidate.content().parts() != null && !candidate.content().parts().isEmpty()) {
                return candidate.content().parts().getFirst().text();
            }
        }
        return null;
    }
}
