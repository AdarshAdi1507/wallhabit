package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val nameInput: String = "",
    val isSaving: Boolean = false,
    val isNavigateToHome: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(nameInput = name) }
    }

    fun onContinue() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            userPreferencesRepository.saveUserName(name)
            _uiState.update { it.copy(isSaving = false, isNavigateToHome = true) }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(isNavigateToHome = false) }
    }
}

