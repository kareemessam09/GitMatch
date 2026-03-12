package com.kareem.gitmatch.feature.detail

import com.kareem.gitmatch.core.model.FeedCard

data class DetailUiState(
    val card: FeedCard? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
