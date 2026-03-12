package com.kareem.GitMatch.dto.request;

import com.kareem.GitMatch.core.enums.SwipeDirection;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Simplified swipe request from the mobile app.
 * The userId comes from the JWT, and the itemType is determined server-side.
 */
public record MobileSwipeRequest(
    @NotNull UUID itemId,
    @NotNull SwipeDirection direction
) {}
