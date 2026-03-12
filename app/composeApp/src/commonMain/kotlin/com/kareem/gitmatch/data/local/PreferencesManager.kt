package com.kareem.gitmatch.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages local persistent storage using DataStore (file-backed, NOT SharedPreferences).
 * Handles both app settings and cached data for offline use.
 */
class PreferencesManager(
    private val dataStore: DataStore<Preferences>
) {

    // --- App Settings (reactive flows) ---

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }

    val hasPromptedGithubToken: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_GITHUB_TOKEN_PROMPT] ?: false }

    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_DARK_MODE] ?: true }

    val selectedInterests: Flow<Set<String>> = dataStore.data
        .map { prefs ->
            prefs[KEY_INTERESTS]?.split(",")?.toSet() ?: emptySet()
        }

    // --- Auth Token ---

    val authToken: Flow<String?> = dataStore.data
        .map { prefs -> prefs[KEY_AUTH_TOKEN] }

    val isLoggedIn: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_AUTH_TOKEN] != null }

    suspend fun setAuthToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
        }
    }

    suspend fun getAuthTokenOnce(): String? {
        return dataStore.data.first()[KEY_AUTH_TOKEN]
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }
    suspend fun setHasPromptedGithubToken(prompted: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_GITHUB_TOKEN_PROMPT] = prompted
        }
    }
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setInterests(interests: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_INTERESTS] = interests.joinToString(",")
        }
    }

    // --- Local Feed Cache (raw JSON strings) ---

    suspend fun getCachedFeed(): String {
        return dataStore.data.first()[KEY_CACHED_FEED] ?: ""
    }

    suspend fun setCachedFeed(json: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CACHED_FEED] = json
        }
    }

    suspend fun clearCachedFeed() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_CACHED_FEED)
        }
    }

    suspend fun getCachedVault(): String {
        return dataStore.data.first()[KEY_CACHED_VAULT] ?: ""
    }

    suspend fun setCachedVault(json: String) {
        dataStore.edit { prefs ->
            prefs[KEY_CACHED_VAULT] = json
        }
    }

    suspend fun clearCachedVault() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_CACHED_VAULT)
        }
    }

    companion object {
        // Settings keys
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_GITHUB_TOKEN_PROMPT = booleanPreferencesKey("github_token_prompt")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_INTERESTS = stringPreferencesKey("interests")

        // Auth keys
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")

        // Local cache keys
        private val KEY_CACHED_FEED = stringPreferencesKey("cached_feed")
        private val KEY_CACHED_VAULT = stringPreferencesKey("cached_vault")
    }
}
