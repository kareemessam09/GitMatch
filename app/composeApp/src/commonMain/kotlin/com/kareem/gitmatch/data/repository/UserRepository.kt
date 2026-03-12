package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.network.GitMatchApi
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.core.network.dto.UserPreferenceRequestDto

interface UserRepository {
    suspend fun getAvailableTopics(): NetworkResult<List<String>>
    suspend fun savePreferences(interests: List<String>): NetworkResult<Unit>
}

class UserRepositoryImpl(
    private val api: GitMatchApi
) : UserRepository {

    override suspend fun getAvailableTopics(): NetworkResult<List<String>> {
        return runCatching {
            api.getAvailableTopics()
        }.fold(
            onSuccess = { NetworkResult.Success(it) },
            onFailure = { NetworkResult.Error(it.message ?: "Failed to fetch topics") }
        )
    }

    override suspend fun savePreferences(interests: List<String>): NetworkResult<Unit> {
        return runCatching {
            api.saveUserPreferences(UserPreferenceRequestDto(preferredTopics = interests))
        }.fold(
            onSuccess = { NetworkResult.Success(Unit) },
            onFailure = { NetworkResult.Error(it.message ?: "Failed to save preferences") }
        )
    }
}
