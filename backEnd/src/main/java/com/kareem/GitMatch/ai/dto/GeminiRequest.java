package com.kareem.GitMatch.ai.dto;

import java.util.List;
import java.util.Map;


public record GeminiRequest(List<Content> contents) {

    public record Content(List<Part> parts) {}

    public record Part(String text) {}


    public static GeminiRequest fromPrompt(String prompt) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(prompt))))
        );
    }
}
