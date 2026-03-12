package com.kareem.gitmatch.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Request body for setting a GitHub Personal Access Token (Google-auth users).
 */
@Serializable
data class GitHubTokenRequestDto(
    val githubToken: String,
    val githubUsername: String? = null
)
