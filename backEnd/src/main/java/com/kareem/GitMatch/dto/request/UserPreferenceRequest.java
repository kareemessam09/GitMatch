package com.kareem.GitMatch.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;


public record UserPreferenceRequest(
    UUID userId,
    List<String> preferredLanguages,
    List<String> preferredTopics
) {}
