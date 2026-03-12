package com.kareem.gitmatch.core.network

import com.kareem.gitmatch.core.network.dto.FeedCardDto
import com.kareem.gitmatch.core.network.dto.GitHubTokenRequestDto
import com.kareem.gitmatch.core.network.dto.SwipeRequestDto
import com.kareem.gitmatch.core.network.dto.UserPreferenceRequestDto
import com.kareem.gitmatch.core.network.dto.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class GitMatchApi(private val client: HttpClient) {

    suspend fun getDiscoverFeed(page: Int = 0, size: Int = 20): List<FeedCardDto> {
        return client.get("feed-cards") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getVaultItems(): List<FeedCardDto> {
        return client.get("feed-cards/vault").body()
    }

    suspend fun getCardDetail(cardId: String): FeedCardDto {
        return client.get("feed-cards/$cardId").body()
    }

    suspend fun recordSwipe(request: SwipeRequestDto) {
        client.post("swipes") {
            setBody(request)
        }
    }

    /**
     * Fetches the master list of available topics from the backend.
     */
    suspend fun getAvailableTopics(): List<String> {
        return client.get("users/available-topics").body()
    }

    suspend fun saveUserPreferences(request: UserPreferenceRequestDto) {
        client.post("users/preferences") {
            setBody(request)
        }
    }

    // --- Auth / Profile ---

    /**
     * Fetches the authenticated user's profile using the JWT in the Authorization header.
     */
    suspend fun getCurrentUser(): UserProfileDto {
        return client.get("users/me").body()
    }

    /**
     * Allows a Google-authenticated user to save a GitHub Personal Access Token
     * for the auto-star feature.
     */
    suspend fun setGithubToken(request: GitHubTokenRequestDto) {
        client.put("users/me/github-token") {
            setBody(request)
        }
    }
}
