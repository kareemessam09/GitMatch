package com.kareem.gitmatch.feature.vault

import com.kareem.gitmatch.core.model.FeedCard

data class VaultUiState(
    val savedCards: List<FeedCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
