package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.UserProfile
import com.kareem.gitmatch.core.network.GitMatchApi
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.core.network.dto.GitHubTokenRequestDto
import com.kareem.gitmatch.core.network.dto.toDomain
import com.kareem.gitmatch.data.local.PreferencesManager

class AuthRepositoryImpl(
    private val api: GitMatchApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun saveToken(token: String) {
        preferencesManager.setAuthToken(token)
    }

    override suspend fun isLoggedIn(): Boolean {
        return preferencesManager.getAuthTokenOnce() != null
    }

    override suspend fun getCurrentUser(): NetworkResult<UserProfile> {
        return runCatching {
            api.getCurrentUser().toDomain()
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { NetworkResult.Error(it.message ?: "Failed to fetch profile") }
        )
    }

    override suspend fun setGithubToken(token: String, username: String?): NetworkResult<Unit> {
        return runCatching {
            api.setGithubToken(GitHubTokenRequestDto(githubToken = token, githubUsername = username))
        }.fold(
            onSuccess = { NetworkResult.Success(Unit) },
            onFailure = { NetworkResult.Error(it.message ?: "Failed to save GitHub token") }
        )
    }

    override suspend fun logout() {
        preferencesManager.clearAuthToken()
    }
}
