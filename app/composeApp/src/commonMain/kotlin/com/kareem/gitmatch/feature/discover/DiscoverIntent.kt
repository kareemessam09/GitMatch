package com.kareem.gitmatch.feature.discover

import com.kareem.gitmatch.core.model.FeedTab

sealed interface DiscoverIntent {
    data class SwipeRight(val cardId: String) : DiscoverIntent
    data class SwipeLeft(val cardId: String) : DiscoverIntent
    data class SwipeUp(val cardId: String) : DiscoverIntent
    data class SelectTab(val tab: FeedTab) : DiscoverIntent
    data object LoadNextPage : DiscoverIntent
    data object Refresh : DiscoverIntent
    data object DismissGithubTokenPrompt : DiscoverIntent
    data object NavigateToSettingsFromPrompt : DiscoverIntent
}
