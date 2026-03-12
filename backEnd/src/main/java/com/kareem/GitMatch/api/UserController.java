package com.kareem.GitMatch.api;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.repository.AppUserRepository;
import com.kareem.GitMatch.dto.request.UserPreferenceRequest;
import com.kareem.GitMatch.dto.response.UserProfileResponse;
import com.kareem.GitMatch.exception.ResourceNotFoundException;
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
    public ResponseEntity<List<String>> getAvailableTopics() {
        return ResponseEntity.ok(AVAILABLE_TOPICS);
    }

    /**
     * Gets a user's profile by ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AppUser> getUser(@PathVariable UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));
        return ResponseEntity.ok(user);
    }

    /**
     * Updates the authenticated user's preferred topics.
     * Accepts the format: { "preferredTopics": ["Android", "Kotlin", ...] }
     */
    @PutMapping("/preferences")
    public ResponseEntity<AppUser> updatePreferences(
            @AuthenticationPrincipal UUID userId,
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
    public ResponseEntity<AppUser> savePreferences(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UserPreferenceRequest request) {
        return updatePreferences(userId, request);
    }

    /**
     * Returns the authenticated user's profile (from the JWT).
     * The mobile app calls this after login to get the user's info.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", "id", userId));
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    /**
     * Allows a Google-authenticated user to manually provide a GitHub Personal Access Token
     * so they can still use the auto-star feature without GitHub OAuth.
     */
    @PutMapping("/me/github-token")
    public ResponseEntity<Map<String, String>> setGithubToken(
            @AuthenticationPrincipal UUID userId,
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
