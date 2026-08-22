package com.kareem.GitMatch.api;

import com.kareem.GitMatch.core.entity.SwipeAction;
import com.kareem.GitMatch.dto.request.MobileSwipeRequest;
import com.kareem.GitMatch.service.SwipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/swipes")
@Tag(name = "Swipes", description = "Swipe action endpoints for liking/ignoring content")
@SecurityRequirement(name = "Bearer Authentication")
public class SwipeController {

    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }


    @PostMapping
    @Operation(summary = "Record swipe", description = "Records a new swipe action (RIGHT = like, LEFT = ignore, UP = more info)")
    public ResponseEntity<SwipeAction> recordSwipe(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody MobileSwipeRequest request) {
        SwipeAction saved = swipeService.recordSwipe(userId, request.itemId(), request.direction());
        return ResponseEntity.created(URI.create("/api/v1/swipes/" + saved.getId())).body(saved);
    }


    @GetMapping("/liked")
    @Operation(summary = "Get liked items", description = "Returns the authenticated user's right-swiped items (their Vault)")
    public ResponseEntity<Page<SwipeAction>> getLikedItems(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(swipeService.getLikedItems(userId, page, size));
    }
}
