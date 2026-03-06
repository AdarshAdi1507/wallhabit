package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.core.notification.AlarmScheduler
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.HabitCategory
import com.dev.habitwallpaper.domain.model.TrackingType
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
    private val alarmScheduler: AlarmScheduler,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUIState())
    val uiState: StateFlow<HabitUIState> = _uiState.asStateFlow()

    private val _customDuration = MutableStateFlow("")
    val customDuration: StateFlow<String> = _customDuration.asStateFlow()

    fun onHabitNameChange(newName: String) {
        _uiState.update { it.copy(habitName = newName, error = null) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onCategoryChange(newCategory: HabitCategory) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onDurationChange(days: Int) {
        _uiState.update { it.copy(durationDays = days) }
    }

    fun onCustomDurationChange(daysString: String) {
        val filtered = daysString.filter { it.isDigit() }
        _customDuration.value = filtered
        val days = filtered.toIntOrNull()
        if (days != null) {
            val clampedDays = days.coerceIn(1, 365)
            _uiState.update { it.copy(durationDays = clampedDays) }
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
                reminderDays = if (enabled && it.reminderDays.isEmpty()) DayOfWeek.entries.toList() else it.reminderDays
            ) 
        }
    }

    fun onReminderModeChange(isDaily: Boolean) {
        _uiState.update { 
            it.copy(
                isDaily = isDaily,
                reminderDays = if (isDaily) DayOfWeek.entries.toList() else emptyList()
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
            state.copy(reminderDays = currentDays, isDaily = currentDays.size == 7)
        }
    }

    fun onReminderTimeChange(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun onTrackingTypeChange(type: TrackingType) {
        _uiState.update { it.copy(trackingType = type) }
    }

    fun onGoalValueChange(value: Float) {
        _uiState.update { it.copy(goalValue = value) }
    }

    fun onColorChange(color: Int?) {
        _uiState.update { it.copy(color = color) }
    }

    fun onIconChange(icon: String?) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun onWallpaperSelectedChange(selected: Boolean) {
        _uiState.update { it.copy(isWallpaperSelected = selected) }
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

        if (currentState.isReminderEnabled && currentState.reminderDays.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one day for reminders") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val habit = Habit(
                    name = currentState.habitName,
                    description = currentState.description,
                    category = currentState.category,
                    durationDays = currentState.durationDays,
                    startDate = currentState.startDate,
                    reminderTime = if (currentState.isReminderEnabled) currentState.reminderTime ?: LocalTime.of(9, 0) else null,
                    reminderDays = if (currentState.isReminderEnabled) currentState.reminderDays else emptyList(),
                    trackingType = currentState.trackingType,
                    goalValue = currentState.goalValue,
                    color = currentState.color,
                    icon = currentState.icon,
                    isWallpaperSelected = currentState.isWallpaperSelected
                )
                val id = createHabitUseCase(habit)
                
                if (habit.reminderTime != null) {
                    alarmScheduler.scheduleHabitReminders(habit.copy(id = id))
                }

                if (habit.isWallpaperSelected) {
                    triggerWallpaperUpdate()
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save habit") }
            }
        }
    }

    private fun triggerWallpaperUpdate() {
        val intent = Intent("com.dev.habitwallpaper.UPDATE_WALLPAPER")
        context.sendBroadcast(intent)
    }
}
