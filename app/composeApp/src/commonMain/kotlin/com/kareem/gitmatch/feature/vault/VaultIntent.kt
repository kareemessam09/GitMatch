package com.kareem.gitmatch.feature.vault

sealed interface VaultIntent {
    data object LoadVault : VaultIntent
    data object Refresh : VaultIntent
    data class OpenDetail(val cardId: String) : VaultIntent
}
