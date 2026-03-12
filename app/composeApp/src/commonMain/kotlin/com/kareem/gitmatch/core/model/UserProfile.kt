package com.kareem.gitmatch.core.model

/**
 * Domain model representing the authenticated user's profile.
 * Mapped from UserProfileDto — never contains raw tokens.
 */
data class UserProfile(
    val id: String,
    val githubUsername: String?,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val authProvider: AuthProvider?,
    val hasGithubToken: Boolean,
    val preferredLanguages: List<String>,
    val preferredTopics: List<String>
)
