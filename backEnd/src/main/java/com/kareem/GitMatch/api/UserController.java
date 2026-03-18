package com.kareem.GitMatch.api;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import com.kareem.GitMatch.dto.request.UserPreferenceRequest;
import com.kareem.GitMatch.dto.response.UserProfileResponse;
import com.kareem.GitMatch.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles user profile, preferences, and authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and preference management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    /**
     * Master list of available topics/interests.
     * The mobile app fetches this list for onboarding — single source of truth.
     */
    public static final List<String> AVAILABLE_TOPICS = List.of(
            "Android", "iOS", "Kotlin", "Java", "Python",
            "Rust", "Go", "TypeScript", "React", "AI/ML",
            "DevOps", "Web3", "Flutter", "Swift", "C++",
            "Cloud", "Security", "Data Science"
    );

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Returns the master list of available topics for onboarding.
     */
    @GetMapping("/available-topics")
    @Operation(summary = "Get available topics", description = "Returns the master list of available topics for onboarding", security = {})
    public ResponseEntity<List<String>> getAvailableTopics() {
        return ResponseEntity.ok(AVAILABLE_TOPICS);
    }

    /**
     * Gets a user's profile by ID.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Gets a user's profile by ID")
    public ResponseEntity<AppUser> getUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));
        return ResponseEntity.ok(user);
    }

    /**
     * Updates the authenticated user's preferred topics.
     * Accepts the format: { "preferredTopics": ["Android", "Kotlin", ...] }
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update preferences", description = "Updates the authenticated user's preferred languages and topics")
    public ResponseEntity<AppUser> updatePreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UserPreferenceRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));

        if (request.preferredLanguages() != null) {
            user.setPreferredLanguages(String.join(",", request.preferredLanguages()));
        }
        if (request.preferredTopics() != null) {
            user.setPreferredTopics(String.join(",", request.preferredTopics()));
        }

        AppUser saved = appUserRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    /**
     * Convenience POST endpoint for the mobile app onboarding flow.
     * Accepts: { "preferredTopics": ["Android", "Kotlin", ...] }
     */
    @PostMapping("/preferences")
    @Operation(summary = "Save preferences", description = "Convenience POST endpoint for the mobile app onboarding flow")
    public ResponseEntity<AppUser> savePreferences(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UserPreferenceRequest request) {
        return updatePreferences(userId, request);
    }

    /**
     * Returns the authenticated user's profile (from the JWT).
     * The mobile app calls this after login to get the user's info.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile from the JWT")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    /**
     * Allows a Google-authenticated user to manually provide a GitHub Personal Access Token
     * so they can still use the auto-star feature without GitHub OAuth.
     */
    @PutMapping("/me/github-token")
    @Operation(summary = "Set GitHub token", description = "Allows a Google-authenticated user to manually provide a GitHub Personal Access Token")
    public ResponseEntity<Map<String, String>> setGithubToken(
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @RequestBody Map<String, String> body) {

        String token = body.get("githubToken");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "githubToken is required"));
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));

        user.setGithubAccessToken(token);

        // If user also provides a GitHub username, save it
        String username = body.get("githubUsername");
        if (username != null && !username.isBlank()) {
            user.setGithubUsername(username);
        }

        appUserRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "GitHub token saved successfully"));
    }
}
