package com.kareem.GitMatch.api;

import com.kareem.GitMatch.core.entity.SwipeAction;
import com.kareem.GitMatch.dto.request.MobileSwipeRequest;
import com.kareem.GitMatch.service.SwipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Handles swipe actions (right/left/up) from the mobile app.
 * The user ID is always extracted from the JWT — never from the request body.
 */
@RestController
@RequestMapping("/api/v1/swipes")
public class SwipeController {

    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }

    /**
     * Records a new swipe action.
     * The userId comes from the JWT, and the itemType is determined server-side.
     */
    @PostMapping
    public ResponseEntity<SwipeAction> recordSwipe(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MobileSwipeRequest request) {
        SwipeAction saved = swipeService.recordSwipe(userId, request.itemId(), request.direction());
        return ResponseEntity.created(URI.create("/api/v1/swipes/" + saved.getId())).body(saved);
    }

    /**
     * Returns the authenticated user's right-swiped items (their "Vault").
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<SwipeAction>> getLikedItems(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(swipeService.getLikedItems(userId, page, size));
    }
}
