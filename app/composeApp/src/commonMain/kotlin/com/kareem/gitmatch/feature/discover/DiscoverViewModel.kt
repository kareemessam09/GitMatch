package com.kareem.gitmatch.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.model.FeedTab
import com.kareem.gitmatch.core.model.SwipeDirection
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.local.PreferencesManager
import com.kareem.gitmatch.data.repository.AuthRepository
import com.kareem.gitmatch.data.repository.FeedRepository
import com.kareem.gitmatch.data.repository.SwipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val feedRepository: FeedRepository,
    private val swipeRepository: SwipeRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var hasPromptedForGithub = false

    init {
        viewModelScope.launch {
            preferencesManager.hasPromptedGithubToken.collect { prompted ->
                hasPromptedForGithub = prompted
            }
        }
        loadFeed()
    }

    fun onIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.SwipeRight -> handleSwipeRight(intent.cardId)
            is DiscoverIntent.SwipeLeft -> handleSwipeLeft(intent.cardId)
            is DiscoverIntent.SwipeUp -> handleSwipeUp(intent.cardId)
            is DiscoverIntent.SelectTab -> filterByTab(intent.tab)
            is DiscoverIntent.LoadNextPage -> loadFeed()
            is DiscoverIntent.Refresh -> refresh()
            is DiscoverIntent.DismissGithubTokenPrompt -> {
                _uiState.update { it.copy(showGithubTokenPrompt = false) }
            }
            is DiscoverIntent.NavigateToSettingsFromPrompt -> {
                _uiState.update { it.copy(showGithubTokenPrompt = false) }
                // App.kt or navigation will handle this. Wait, UI side effect might be needed.
                // We'll manage it by letting the UI trigger the callback.
            }
        }
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = feedRepository.getDiscoverFeed(currentPage, DEFAULT_PAGE_SIZE)) {
                is NetworkResult.Success -> {
                    val newCards = result.data
                    val existingIds = _uiState.value.cards.map { it.id }.toSet()
                    val uniqueNewCards = newCards.filter { it.id !in existingIds }
                    val newRepoCount = uniqueNewCards.count {
                        it.type == com.kareem.gitmatch.core.model.FeedItemType.REPO
                    }
                    val newNewsCount = uniqueNewCards.count {
                        it.type == com.kareem.gitmatch.core.model.FeedItemType.NEWS
                    }
                    currentPage++
                    _uiState.update { state ->
                        state.copy(
                            cards = state.cards + uniqueNewCards,
                            isLoading = false,
                            hasMoreRepos = newRepoCount > 0,
                            hasMoreNews = newNewsCount > 0
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun refresh() {
        currentPage = 0
        _uiState.update { DiscoverUiState(isRefreshing = true) }
        viewModelScope.launch {
            when (val result = feedRepository.getDiscoverFeed(0, DEFAULT_PAGE_SIZE)) {
                is NetworkResult.Success -> {
                    val newCards = result.data
                    val newRepoCount = newCards.count {
                        it.type == com.kareem.gitmatch.core.model.FeedItemType.REPO
                    }
                    val newNewsCount = newCards.count {
                        it.type == com.kareem.gitmatch.core.model.FeedItemType.NEWS
                    }
                    currentPage = 1
                    _uiState.update {
                        DiscoverUiState(
                            cards = newCards,
                            isRefreshing = false,
                            hasMoreRepos = newRepoCount > 0,
                            hasMoreNews = newNewsCount > 0
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message, isRefreshing = false) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun handleSwipeRight(cardId: String) {
        // Advance immediately for responsive UI
        advanceCard(cardId)
        viewModelScope.launch {
            val card = _uiState.value.cards.find { it.id == cardId }
            if (card?.type == com.kareem.gitmatch.core.model.FeedItemType.REPO && !hasPromptedForGithub) {
                val userResult = authRepository.getCurrentUser()
                if (userResult is NetworkResult.Success) {
                    val user = userResult.data
                    if (user.authProvider == com.kareem.gitmatch.core.model.AuthProvider.GOOGLE && !user.hasGithubToken) {
                        hasPromptedForGithub = true
                        preferencesManager.setHasPromptedGithubToken(true)
                        _uiState.update { it.copy(showGithubTokenPrompt = true) }
                    }
                }
            }
            // Record swipe in background — card already advanced
            swipeRepository.recordSwipe(cardId, SwipeDirection.RIGHT)
        }
    }

    private fun handleSwipeLeft(cardId: String) {
        advanceCard(cardId)
        viewModelScope.launch {
            swipeRepository.recordSwipe(cardId, SwipeDirection.LEFT)
        }
    }

    private fun handleSwipeUp(cardId: String) {
        // Swipe up navigates to detail — handled by the screen via navigation callback
        advanceCard(cardId)
        viewModelScope.launch {
            swipeRepository.recordSwipe(cardId, SwipeDirection.UP)
        }
    }

    private fun advanceCard(cardId: String) {
        _uiState.update { state ->
            state.copy(swipedCardIds = state.swipedCardIds + cardId)
        }
        // Load more cards when running low on unswiped cards
        val state = _uiState.value
        val remaining = state.cards.count { it.id !in state.swipedCardIds }
        val hasMore = state.hasMoreRepos || state.hasMoreNews
        if (remaining <= 5 && hasMore) {
            loadFeed()
        }
    }

    private fun filterByTab(tab: FeedTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
}
