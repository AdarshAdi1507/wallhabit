package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import java.time.LocalDate

data class WallpaperState(
    val habitName: String,
    val streakCount: Int,
    val completionGrid: List<GridCellState>
)

data class GridCellState(
    val isCompleted: Boolean = false,
    val isToday: Boolean = false,
    val isFuture: Boolean = false,
    val isPadding: Boolean = false
)

class GenerateWallpaperStateUseCase {
    operator fun invoke(habit: Habit?): WallpaperState? {
        if (habit == null) return null
        
        val today = LocalDate.now()
        val firstDayOfWeek = habit.startDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val paddingDays = firstDayOfWeek - 1
        
        val grid = mutableListOf<GridCellState>()
        
        // Add padding for the first week to align with Monday
        repeat(paddingDays) {
            grid.add(GridCellState(isPadding = true))
        }
        
        // Add actual habit days
        for (i in 0 until habit.durationDays) {
            val date = habit.startDate.plusDays(i.toLong())
            grid.add(
                GridCellState(
                    isCompleted = habit.completedDates.contains(date),
                    isToday = date == today,
                    isFuture = date.isAfter(today)
                )
            )
        }
        
        return WallpaperState(
            habitName = habit.name,
            streakCount = habit.currentStreak,
            completionGrid = grid
        )
    }
}
