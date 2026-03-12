package com.kareem.gitmatch.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kareem.gitmatch.core.network.NetworkResult
import com.kareem.gitmatch.data.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VaultViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadVault()
    }

    fun onIntent(intent: VaultIntent) {
        when (intent) {
            is VaultIntent.LoadVault -> loadVault()
            is VaultIntent.Refresh -> loadVault()
            is VaultIntent.OpenDetail -> { /* handled by navigation callback */ }
        }
    }

    private fun loadVault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = feedRepository.getVaultItems()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(savedCards = result.data, isLoading = false)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(error = result.message, isLoading = false)
                    }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}
