package com.dev.habitwallpaper.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: HabitCategory = HabitCategory.GENERAL,
    val durationDays: Int,
    val startDate: LocalDate,
    val reminderTime: LocalTime? = null,
    val reminderDays: List<DayOfWeek> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isCompletedToday: Boolean = false,
    val currentStreak: Int = 0,
    val completedDates: List<LocalDate> = emptyList(),
    val isWallpaperSelected: Boolean = false,
    val trackingType: TrackingType = TrackingType.BINARY,
    val goalValue: Float = 1f,
    val color: Int? = null,
    val icon: String? = null
)
