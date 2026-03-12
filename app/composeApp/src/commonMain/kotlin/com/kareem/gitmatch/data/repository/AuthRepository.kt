package com.kareem.gitmatch.data.repository

import com.kareem.gitmatch.core.model.UserProfile
import com.kareem.gitmatch.core.network.NetworkResult

/**
 * Manages authentication state and user profile retrieval.
 */
interface AuthRepository {

    /**
     * Saves the JWT token received from the OAuth deep link.
     */
    suspend fun saveToken(token: String)

    /**
     * Checks whether a JWT token is stored locally.
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Fetches the authenticated user's profile from the backend.
     */
    suspend fun getCurrentUser(): NetworkResult<UserProfile>

    /**
     * Saves a GitHub Personal Access Token for Google-authenticated users.
     */
    suspend fun setGithubToken(token: String, username: String?): NetworkResult<Unit>

    /**
     * Clears the stored JWT token (log out).
     */
    suspend fun logout()
}
