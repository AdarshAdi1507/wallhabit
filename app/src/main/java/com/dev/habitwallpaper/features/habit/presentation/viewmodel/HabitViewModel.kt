package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.core.notification.AlarmScheduler
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.usecase.CreateHabitUseCase
import com.dev.habitwallpaper.features.habit.presentation.state.HabitUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class HabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase,
    private val alarmScheduler: AlarmScheduler
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

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isReminderEnabled = enabled,
                reminderTime = if (enabled) it.reminderTime ?: LocalTime.of(9, 0) else null,
                reminderDays = if (enabled && it.isDaily) DayOfWeek.entries else it.reminderDays
            ) 
        }
    }

    fun onReminderModeChange(isDaily: Boolean) {
        _uiState.update { 
            it.copy(
                isDaily = isDaily,
                reminderDays = if (isDaily) DayOfWeek.entries else emptyList()
            ) 
        }
    }

    fun toggleReminderDay(day: DayOfWeek) {
        _uiState.update { state ->
            val currentDays = state.reminderDays.toMutableList()
            if (currentDays.contains(day)) {
                currentDays.remove(day)
            } else {
                currentDays.add(day)
            }
            state.copy(reminderDays = currentDays)
        }
    }

    fun onReminderTimeChange(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time) }
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

        if (currentState.isReminderEnabled && !currentState.isDaily && currentState.reminderDays.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day for reminders") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val habit = Habit(
                    name = currentState.habitName,
                    durationDays = currentState.durationDays,
                    startDate = currentState.startDate,
                    reminderTime = if (currentState.isReminderEnabled) currentState.reminderTime else null,
                    reminderDays = if (currentState.isReminderEnabled) currentState.reminderDays else emptyList()
                )
                val id = createHabitUseCase(habit)
                
                if (habit.reminderTime != null) {
                    alarmScheduler.scheduleHabitReminders(habit.copy(id = id))
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save habit") }
            }
        }
    }
}
