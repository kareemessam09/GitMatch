package com.kareem.gitmatch.core.network.dto

import kotlinx.serialization.Serializable

/**
 * DTO matching the backend's UserProfileResponse.
 */
@Serializable
data class UserProfileDto(
    val id: String,
    val githubUsername: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val authProvider: String? = null,
    val hasGithubToken: Boolean = false,
    val preferredLanguages: List<String> = emptyList(),
    val preferredTopics: List<String> = emptyList()
)
