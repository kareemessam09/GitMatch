package com.kareem.gitmatch.feature.settings

import com.kareem.gitmatch.core.model.AuthProvider
import com.kareem.gitmatch.core.model.UserProfile

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val selectedInterests: Set<String> = emptySet(),
    val userProfile: UserProfile? = null,
    val isLoadingProfile: Boolean = false,
    val githubTokenInput: String = "",
    val githubUsernameInput: String = "",
    val isSavingToken: Boolean = false,
    val tokenSaveMessage: String? = null,
    val isLoggedOut: Boolean = false,
    // Topic editing
    val isEditingTopics: Boolean = false,
    val availableTopics: List<String> = emptyList(),
    val editingInterests: Set<String> = emptySet(),
    val isSavingTopics: Boolean = false
) {
    /** True when the user signed in with Google and has no GitHub token yet */
    val showGithubTokenSection: Boolean
        get() = userProfile?.authProvider == AuthProvider.GOOGLE

    val displayName: String
        get() = userProfile?.displayName ?: userProfile?.githubUsername ?: "User"

    val displayEmail: String
        get() = userProfile?.email ?: ""
}

sealed interface SettingsIntent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsIntent
    data class UpdateGithubTokenInput(val token: String) : SettingsIntent
    data class UpdateGithubUsernameInput(val username: String) : SettingsIntent
    data object SaveGithubToken : SettingsIntent
    data object Logout : SettingsIntent
    data object DismissTokenMessage : SettingsIntent
    // Topic editing intents
    data object StartEditingTopics : SettingsIntent
    data object CancelEditingTopics : SettingsIntent
    data class ToggleEditingTopic(val topic: String) : SettingsIntent
    data object SaveTopics : SettingsIntent
}
