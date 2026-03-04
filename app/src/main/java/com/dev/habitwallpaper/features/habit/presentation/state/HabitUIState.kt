package com.dev.habitwallpaper.features.habit.presentation.state

import java.time.LocalDate

data class HabitUIState(
    val habitName: String = "",
    val durationDays: Int = 30,
    val startDate: LocalDate = LocalDate.now(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
