package com.kareem.gitmatch.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val cardId: String,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadCardDetail()
    }

    private fun loadCardDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = feedRepository.getCardDetail(cardId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(card = result.data, isLoading = false) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}
