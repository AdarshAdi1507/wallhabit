package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.usecase.CreateHabitUseCase
import com.dev.habitwallpaper.features.habit.presentation.state.HabitUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUIState())
    val uiState: StateFlow<HabitUIState> = _uiState.asStateFlow()

    private val _customDuration = MutableStateFlow("")
    val customDuration: StateFlow<String> = _customDuration.asStateFlow()

    fun onHabitNameChange(newName: String) {
        _uiState.update { it.copy(habitName = newName, error = null) }
    }

    fun onDurationChange(days: Int) {
        _uiState.update { it.copy(durationDays = days) }
    }

    fun onCustomDurationChange(daysString: String) {
        _customDuration.value = daysString
        val days = daysString.toIntOrNull()
        if (days != null) {
            _uiState.update { it.copy(durationDays = days) }
        }
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun saveHabit() {
        val currentState = _uiState.value
        if (currentState.habitName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a habit name") }
            return
        }
        
        if (currentState.durationDays <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid duration") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val habit = Habit(
                    name = currentState.habitName,
                    durationDays = currentState.durationDays,
                    startDate = currentState.startDate
                )
                createHabitUseCase(habit)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save habit") }
            }
        }
    }
}
