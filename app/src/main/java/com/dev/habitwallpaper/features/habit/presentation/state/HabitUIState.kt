package com.dev.habitwallpaper.features.habit.presentation.state

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class HabitUIState(
    val habitName: String = "",
    val durationDays: Int = 30,
    val startDate: LocalDate = LocalDate.now(),
    val reminderTime: LocalTime? = null,
    val reminderDays: List<DayOfWeek> = emptyList(),
    val isReminderEnabled: Boolean = false,
    val isDaily: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
