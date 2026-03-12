package com.kareem.gitmatch.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.network.ApiConfig
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.local.PreferencesManager
import com.kareem.gitmatch.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Handles login state. The actual OAuth browser flow is triggered
 * by the platform (Android Activity / iOS UIApplication).
 * This ViewModel only manages the token received from the deep link callback.
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Returns the full OAuth URL for the given provider.
     * The app opens this in a Chrome Custom Tab / Safari View Controller.
     */
    fun getOAuthUrl(provider: String): String {
        return "${ApiConfig.AUTH_BASE_URL}/oauth2/authorization/${provider.lowercase()}"
    }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SignInWithGitHub -> {
                // URL is retrieved via getOAuthUrl("github") by the UI layer
                // The actual browser launch is platform-specific
            }

            is LoginIntent.SignInWithGoogle -> {
                // URL is retrieved via getOAuthUrl("google") by the UI layer
            }

            is LoginIntent.HandleDeepLinkToken -> {
                handleToken(intent.token)
            }

            is LoginIntent.UpdateManualToken -> {
                _uiState.update { it.copy(manualToken = intent.token) }
            }

            is LoginIntent.SubmitManualToken -> {
                val tokenToSubmit = _uiState.value.manualToken.trim()
                if (tokenToSubmit.isNotEmpty()) {
                    // Extract token if user pasted the full URL (e.g. gitmatch://login?token=XYZ)
                    val actualToken = if (tokenToSubmit.contains("token=")) {
                        tokenToSubmit.substringAfter("token=").substringBefore("&")
                    } else {
                        tokenToSubmit
                    }
                    handleToken(actualToken)
                    _uiState.update { it.copy(manualToken = "") }
                }
            }

            is LoginIntent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun handleToken(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                authRepository.saveToken(token)

                // Fetch user data and update onboarding state
                val result = authRepository.getCurrentUser()
                if (result is NetworkResult.Success) {
                    val hasOnboarded = result.data.preferredTopics?.isNotEmpty() == true
                    preferencesManager.setOnboardingCompleted(hasOnboarded)
                }

                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to process login"
                    )
                }
            }
        }
    }
}
