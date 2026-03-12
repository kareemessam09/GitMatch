package com.kareem.gitmatch.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Discover : Screen

    @Serializable
    data object Vault : Screen

    @Serializable
    data class Detail(val cardId: String) : Screen

    @Serializable
    data object Settings : Screen
}
