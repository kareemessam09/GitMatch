package com.kareem.gitmatch.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.local.PreferencesManager
import com.kareem.gitmatch.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadAvailableTopics()
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.ToggleLanguage -> toggleLanguage(intent.language)
            is OnboardingIntent.ToggleTopic -> toggleTopic(intent.topic)
            is OnboardingIntent.NextStep -> nextStep()
            is OnboardingIntent.PreviousStep -> previousStep()
            is OnboardingIntent.Continue -> saveAndContinue()
        }
    }

    private fun loadAvailableTopics() {
        viewModelScope.launch {
            when (val result = userRepository.getAvailableTopics()) {
                is NetworkResult.Success -> {
                    // The backend returns a combined list; split into languages & topics
                    // Keep default lists as they are well-categorized
                }
                is NetworkResult.Error -> {
                    // Keep the default hardcoded lists as fallback
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun toggleLanguage(language: String) {
        _uiState.update { state ->
            val newSet = state.selectedLanguages.toMutableSet()
            if (language in newSet) newSet.remove(language) else newSet.add(language)
            state.copy(selectedLanguages = newSet)
        }
    }

    private fun toggleTopic(topic: String) {
        _uiState.update { state ->
            val newSet = state.selectedTopics.toMutableSet()
            if (topic in newSet) newSet.remove(topic) else newSet.add(topic)
            state.copy(selectedTopics = newSet)
        }
    }

    private fun nextStep() {
        _uiState.update { it.copy(currentStep = 1) }
    }

    private fun previousStep() {
        _uiState.update { it.copy(currentStep = 0) }
    }

    private fun saveAndContinue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val allInterests = (_uiState.value.selectedLanguages + _uiState.value.selectedTopics).toList()

            when (val result = userRepository.savePreferences(allInterests)) {
                is NetworkResult.Success -> {
                    preferencesManager.setOnboardingCompleted(true)
                    preferencesManager.setInterests(allInterests.toSet())
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is NetworkResult.Error -> {
                    // Save locally even if network fails (graceful degradation)
                    preferencesManager.setOnboardingCompleted(true)
                    preferencesManager.setInterests(allInterests.toSet())
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}
