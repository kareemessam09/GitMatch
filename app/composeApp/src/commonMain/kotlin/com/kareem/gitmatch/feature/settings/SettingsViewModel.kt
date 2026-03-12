package com.kareem.gitmatch.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.local.PreferencesManager
import com.kareem.gitmatch.data.repository.AuthRepository
import com.kareem.gitmatch.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.isDarkMode.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            preferencesManager.selectedInterests.collect { interests ->
                _uiState.update { it.copy(selectedInterests = interests) }
            }
        }
        loadUserProfile()
        loadAvailableTopics()
    }

    private fun loadAvailableTopics() {
        viewModelScope.launch {
            when (val result = userRepository.getAvailableTopics()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(availableTopics = result.data) }
                }
                is NetworkResult.Error -> {
                    // fallback to hardcoded list
                    _uiState.update {
                        it.copy(availableTopics = listOf(
                            "Android", "iOS", "Kotlin", "Java", "Python",
                            "Rust", "Go", "TypeScript", "React", "AI/ML",
                            "DevOps", "Web3", "Flutter", "Swift", "C++",
                            "Cloud", "Security", "Data Science"
                        ))
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            when (val result = authRepository.getCurrentUser()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            userProfile = result.data,
                            isLoadingProfile = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoadingProfile = false) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleDarkMode -> {
                viewModelScope.launch {
                    preferencesManager.setDarkMode(intent.enabled)
                }
            }

            is SettingsIntent.UpdateGithubTokenInput -> {
                _uiState.update { it.copy(githubTokenInput = intent.token) }
            }

            is SettingsIntent.UpdateGithubUsernameInput -> {
                _uiState.update { it.copy(githubUsernameInput = intent.username) }
            }

            is SettingsIntent.SaveGithubToken -> {
                saveGithubToken()
            }

            is SettingsIntent.Logout -> {
                viewModelScope.launch {
                    authRepository.logout()
                    _uiState.update { it.copy(isLoggedOut = true) }
                }
            }

            is SettingsIntent.DismissTokenMessage -> {
                _uiState.update { it.copy(tokenSaveMessage = null) }
            }

            is SettingsIntent.StartEditingTopics -> {
                _uiState.update {
                    it.copy(
                        isEditingTopics = true,
                        editingInterests = it.selectedInterests
                    )
                }
            }

            is SettingsIntent.CancelEditingTopics -> {
                _uiState.update { it.copy(isEditingTopics = false) }
            }

            is SettingsIntent.ToggleEditingTopic -> {
                _uiState.update { state ->
                    val newSet = state.editingInterests.toMutableSet()
                    if (intent.topic in newSet) newSet.remove(intent.topic) else newSet.add(intent.topic)
                    state.copy(editingInterests = newSet)
                }
            }

            is SettingsIntent.SaveTopics -> {
                saveTopics()
            }
        }
    }

    private fun saveTopics() {
        val topics = _uiState.value.editingInterests.toList()
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingTopics = true) }
            when (userRepository.savePreferences(topics)) {
                is NetworkResult.Success, is NetworkResult.Error -> {
                    // Save locally regardless
                    preferencesManager.setInterests(topics.toSet())
                    _uiState.update {
                        it.copy(
                            isSavingTopics = false,
                            isEditingTopics = false,
                            selectedInterests = topics.toSet()
                        )
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun saveGithubToken() {
        val token = _uiState.value.githubTokenInput.trim()
        if (token.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingToken = true) }
            val username = _uiState.value.githubUsernameInput.trim().ifBlank { null }
            when (val result = authRepository.setGithubToken(token, username)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingToken = false,
                            githubTokenInput = "",
                            githubUsernameInput = "",
                            tokenSaveMessage = "GitHub token saved successfully!"
                        )
                    }
                    loadUserProfile() // Refresh profile to update hasGithubToken
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingToken = false,
                            tokenSaveMessage = "Failed: ${result.message}"
                        )
                    }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
