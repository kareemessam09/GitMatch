package com.kareem.gitmatch.feature.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kareem.gitmatch.feature.discover.components.FeedTabRow
import com.kareem.gitmatch.feature.discover.components.SwipeableCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    uiState: DiscoverUiState,
    onIntent: (DiscoverIntent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Row
        FeedTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = { tab -> onIntent(DiscoverIntent.SelectTab(tab)) }
        )

        // Pull-to-refresh wrapping the card deck area
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onIntent(DiscoverIntent.Refresh) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && uiState.cards.isEmpty() -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.error != null && uiState.cards.isEmpty() -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { onIntent(DiscoverIntent.Refresh) }) {
                                Text("Retry")
                            }
                        }
                    }
                    uiState.isEmpty -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.hasMorePages) "You've seen all loaded cards" else "No more cards!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.hasMorePages) "Load more to keep discovering" else "Check back later for fresh discoveries",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (uiState.hasMorePages) {
                                TextButton(onClick = { onIntent(DiscoverIntent.LoadNextPage) }) {
                                    Text("Load More")
                                }
                            }
                            TextButton(onClick = { onIntent(DiscoverIntent.Refresh) }) {
                                Text("Refresh")
                            }
                        }
                    }
                    else -> {
                        // Card stack — render up to 3 cards, bottom-first
                        val visibleCards = uiState.visibleCards
                        visibleCards.asReversed().forEachIndexed { reversedIndex, card ->
                            val stackIndex = visibleCards.size - 1 - reversedIndex

                            // key(card.id) ensures each card has its own animation state
                            key(card.id) {
                                if (stackIndex == 0) {
                                    // Top card — interactive
                                    SwipeableCard(
                                        card = card,
                                        onSwipeRight = { onIntent(DiscoverIntent.SwipeRight(card.id)) },
                                        onSwipeLeft = { onIntent(DiscoverIntent.SwipeLeft(card.id)) },
                                        onSwipeUp = { onNavigateToDetail(card.id) },
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                } else {
                                    // Background cards — same size, hidden behind the top card.
                                    // No scale/translate so the next card doesn't "pop forward"
                                    // when the top card is swiped away.
                                    SwipeableCard(
                                        card = card,
                                        onSwipeRight = {},
                                        onSwipeLeft = {},
                                        onSwipeUp = {},
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (uiState.showGithubTokenPrompt) {
            AlertDialog(
                onDismissRequest = { onIntent(DiscoverIntent.DismissGithubTokenPrompt) },
                title = {
                    Text(text = "Turn on Auto-Star?")
                },
                text = {
                    Text(text = "Add your GitHub token in Settings so repos you swipe right on are automatically starred on your GitHub account! Otherwise, they'll just save to your Vault.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onIntent(DiscoverIntent.NavigateToSettingsFromPrompt)
                            onNavigateToSettings()
                        }
                    ) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onIntent(DiscoverIntent.DismissGithubTokenPrompt) }
                    ) {
                        Text("Not Now")
                    }
                }
            )
        }
    }
}
