package com.kareem.GitMatch.dto.request;

import com.kareem.GitMatch.core.enums.SwipeDirection;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record MobileSwipeRequest(
    @NotNull UUID itemId,
    @NotNull SwipeDirection direction
) {}
