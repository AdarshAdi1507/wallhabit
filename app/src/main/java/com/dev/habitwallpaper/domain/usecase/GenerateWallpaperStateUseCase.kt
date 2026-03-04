package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import java.time.LocalDate

data class WallpaperState(
    val habitName: String,
    val streakCount: Int,
    val completionGrid: List<Boolean> // Last 30 days
)

class GenerateWallpaperStateUseCase {
    operator fun invoke(habit: Habit?): WallpaperState? {
        if (habit == null) return null
        
        val today = LocalDate.now()
        val last30Days = (0..29).reversed().map { today.minusDays(it.toLong()) }
        val grid = last30Days.map { habit.completedDates.contains(it) }
        
        return WallpaperState(
            habitName = habit.name,
            streakCount = habit.currentStreak,
            completionGrid = grid
        )
    }
}
