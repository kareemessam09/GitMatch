package com.kareem.GitMatch.api;

import com.kareem.GitMatch.dto.response.FeedCardResponse;
import com.kareem.GitMatch.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Provides the discovery feed endpoints for the mobile app.
 * All endpoints use the authenticated user's ID from the JWT.
 */
@RestController
@RequestMapping("/api/v1/feed-cards")
@Tag(name = "Feed", description = "Discovery feed endpoints for repositories and news")
@SecurityRequirement(name = "Bearer Authentication")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Returns a personalized discovery feed excluding items the user has already swiped on.
     */
    @GetMapping
    @Operation(summary = "Get personalized feed", description = "Returns a personalized discovery feed excluding items the user has already swiped on")
    public ResponseEntity<List<FeedCardResponse>> getDiscoverFeed(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getPersonalizedFeed(userId, page, size));
    }

    /**
     * Returns the user's vault (right-swiped / liked items).
     */
    @GetMapping("/vault")
    @Operation(summary = "Get vault items", description = "Returns the user's vault (right-swiped / liked items)")
    public ResponseEntity<List<FeedCardResponse>> getVaultItems(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(feedService.getVaultItems(userId));
    }

    /**
     * Returns a single card by its ID.
     */
    @GetMapping("/{cardId}")
    @Operation(summary = "Get card detail", description = "Returns a single feed card by its ID")
    public ResponseEntity<FeedCardResponse> getCardDetail(
            @Parameter(description = "Card ID") @PathVariable UUID cardId) {
        return ResponseEntity.ok(feedService.getCardById(cardId));
    }
}
