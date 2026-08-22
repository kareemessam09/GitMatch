package com.kareem.GitMatch.ai.dto;

import java.util.List;


public record GeminiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}


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
