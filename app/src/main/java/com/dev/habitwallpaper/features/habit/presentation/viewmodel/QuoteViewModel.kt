package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Quote
import com.dev.habitwallpaper.domain.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QuoteUiState {
    data object Loading : QuoteUiState
    data class Success(val quote: Quote) : QuoteUiState
    data object Error : QuoteUiState
}

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    init {
        loadQuote()
    }

    fun loadQuote() {
        viewModelScope.launch {
            _uiState.value = QuoteUiState.Loading
            try {
                val quote = quoteRepository.getRandomQuote()
                _uiState.value = QuoteUiState.Success(quote)
            } catch (_: Exception) {
                _uiState.value = QuoteUiState.Error
            }
        }
    }
}


