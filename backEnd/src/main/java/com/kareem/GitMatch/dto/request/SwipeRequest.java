package com.kareem.GitMatch.dto.request;

import com.kareem.GitMatch.core.enums.FeedItemType;
import com.kareem.GitMatch.core.enums.SwipeDirection;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record SwipeRequest(
    @NotNull UUID userId,
    @NotNull UUID itemId,
    @NotNull FeedItemType itemType,
    @NotNull SwipeDirection direction
) {}
