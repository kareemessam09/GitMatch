package com.kareem.GitMatch.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for updating user preferences (languages, topics).
 * userId is optional — defaults to the demo user if not provided.
 */
public record UserPreferenceRequest(
    UUID userId,
    List<String> preferredLanguages,
    List<String> preferredTopics
) {}
