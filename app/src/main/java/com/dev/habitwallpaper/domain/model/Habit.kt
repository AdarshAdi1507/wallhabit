package com.dev.habitwallpaper.domain.model

import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    val durationDays: Int,
    val startDate: LocalDate,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompletedToday: Boolean = false,
    val currentStreak: Int = 0,
    val completedDates: List<LocalDate> = emptyList(),
    val isWallpaperSelected: Boolean = false
)
