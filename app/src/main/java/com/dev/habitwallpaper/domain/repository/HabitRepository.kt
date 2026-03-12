package com.dev.habitwallpaper.domain.repository

import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    suspend fun insertHabit(habit: Habit): Long
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: Long): Flow<Habit?>
    fun getWallpaperHabit(): Flow<Habit?>
    suspend fun setAsWallpaperHabit(id: Long)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate, value: Float = 1f): Achievement?
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habitId: Long)
    suspend fun pauseHabit(habitId: Long, isPaused: Boolean)
    
    // Achievement related
    fun getAchievementsForHabit(habitId: Long): Flow<List<Achievement>>
    suspend fun recalculateStreak(habitId: Long): Int
}
