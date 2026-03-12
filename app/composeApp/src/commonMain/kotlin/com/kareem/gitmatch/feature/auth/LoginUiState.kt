package com.kareem.gitmatch.feature.auth

/**
 * UI state for the Login screen.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

/**
 * User intents from the Login screen.
 */
sealed interface LoginIntent {
    data object SignInWithGitHub : LoginIntent
    data object SignInWithGoogle : LoginIntent
    data class HandleDeepLinkToken(val token: String) : LoginIntent
    data object DismissError : LoginIntent
}
