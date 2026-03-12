package com.kareem.gitmatch.feature.discover

import com.kareem.gitmatch.core.model.FeedCard
import com.kareem.gitmatch.core.model.FeedTab

data class DiscoverUiState(
    val cards: List<FeedCard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTab: FeedTab = FeedTab.FOR_YOU,
    val showGithubTokenPrompt: Boolean = false,
    val hasMoreRepos: Boolean = true,
    val hasMoreNews: Boolean = true,
    val swipedCardIds: Set<String> = emptySet()
) {
    val visibleCards: List<FeedCard>
        get() {
            val filtered = when (selectedTab) {
                FeedTab.FOR_YOU -> cards
                FeedTab.REPOS -> cards.filter { it.type == com.kareem.gitmatch.core.model.FeedItemType.REPO }
                FeedTab.NEWS -> cards.filter { it.type == com.kareem.gitmatch.core.model.FeedItemType.NEWS }
            }
            return filtered.filter { it.id !in swipedCardIds }.take(3)
        }

    /** Whether more pages are available for the current tab */
    val hasMorePages: Boolean
        get() = when (selectedTab) {
            FeedTab.FOR_YOU -> hasMoreRepos || hasMoreNews
            FeedTab.REPOS -> hasMoreRepos
            FeedTab.NEWS -> hasMoreNews
        }

    val isEmpty: Boolean
        get() = visibleCards.isEmpty() && !isLoading
}
