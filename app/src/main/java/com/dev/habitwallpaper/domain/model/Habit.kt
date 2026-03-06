package com.dev.habitwallpaper.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

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
    val completions: List<HabitCompletion> = emptyList(),
    val isWallpaperSelected: Boolean = false,
    val trackingType: TrackingType = TrackingType.BINARY,
    val goalValue: Float = 1f,
    val color: Int? = null,
    val icon: String? = null
) {
    val completedDates: List<LocalDate> get() = completions.map { it.date }

    val currentDay: Int
        get() = (ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1).toInt().coerceIn(1, durationDays)

    val endDate: LocalDate
        get() = startDate.plusDays(durationDays.toLong() - 1)

    val remainingDays: Int
        get() = ChronoUnit.DAYS.between(LocalDate.now(), endDate).toInt().coerceAtLeast(0)

    val progress: Float
        get() = if (durationDays > 0) (completions.size.toFloat() / durationDays.toFloat()).coerceIn(0f, 1f) else 0f

    val totalCompleted: Int
        get() = completions.size

    val missedDays: Int
        get() {
            val daysSinceStart = (ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1).toInt()
            val expectedDaysSoFar = daysSinceStart.coerceAtMost(durationDays).coerceAtLeast(0)
            return (expectedDaysSoFar - totalCompleted).coerceAtLeast(0)
        }

    val completionRate: Float
        get() {
            val daysSinceStart = (ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1).toInt()
            val totalPossibleSoFar = daysSinceStart.coerceAtMost(durationDays).coerceAtLeast(1)
            return (totalCompleted.toFloat() / totalPossibleSoFar.toFloat()).coerceIn(0f, 1f)
        }

    val longestStreak: Int
        get() {
            if (completions.isEmpty()) return 0
            val sortedDates = completedDates.sorted()
            var maxStreak = 0
            var tempStreak = 1
            
            for (i in 0 until sortedDates.size - 1) {
                if (ChronoUnit.DAYS.between(sortedDates[i], sortedDates[i+1]) == 1L) {
                    tempStreak++
                } else {
                    maxStreak = maxOf(maxStreak, tempStreak)
                    tempStreak = 1
                }
            }
            return maxOf(maxStreak, tempStreak)
        }
}

data class HabitCompletion(
    val date: LocalDate,
    val value: Float
)
