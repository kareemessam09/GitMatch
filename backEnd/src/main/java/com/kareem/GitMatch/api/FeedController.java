package com.kareem.GitMatch.api;

import com.kareem.GitMatch.dto.response.FeedCardResponse;
import com.kareem.GitMatch.service.FeedService;
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
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Returns a personalized discovery feed excluding items the user has already swiped on.
     */
    @GetMapping
    public ResponseEntity<List<FeedCardResponse>> getDiscoverFeed(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getPersonalizedFeed(userId, page, size));
    }

    /**
     * Returns the user's vault (right-swiped / liked items).
     */
    @GetMapping("/vault")
    public ResponseEntity<List<FeedCardResponse>> getVaultItems(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(feedService.getVaultItems(userId));
    }

    /**
     * Returns a single card by its ID.
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<FeedCardResponse> getCardDetail(@PathVariable UUID cardId) {
        return ResponseEntity.ok(feedService.getCardById(cardId));
    }
}
