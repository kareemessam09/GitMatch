package com.kareem.GitMatch.dto.response;

import com.kareem.GitMatch.core.entity.AppUser;
import com.kareem.GitMatch.core.enums.AuthProvider;

import java.util.List;
import java.util.UUID;

/**
 * Safe user profile DTO — never exposes sensitive tokens to the client.
 */
public record UserProfileResponse(
        UUID id,
        String githubUsername,
        String email,
        String displayName,
        String avatarUrl,
        AuthProvider authProvider,
        boolean hasGithubToken,
        List<String> preferredLanguages,
        List<String> preferredTopics
) {

    /**
     * Maps an AppUser entity to a safe profile response.
     */
    public static UserProfileResponse from(AppUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getGithubUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getAuthProvider(),
                user.getGithubAccessToken() != null && !user.getGithubAccessToken().isBlank(),
                parseList(user.getPreferredLanguages()),
                parseList(user.getPreferredTopics())
        );
    }

    private static List<String> parseList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(","));
    }
}
