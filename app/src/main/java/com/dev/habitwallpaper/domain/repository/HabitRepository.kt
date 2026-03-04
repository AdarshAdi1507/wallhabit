package com.dev.habitwallpaper.domain.repository

import com.dev.habitwallpaper.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {
    suspend fun insertHabit(habit: Habit)
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: Long): Flow<Habit?>
    fun getWallpaperHabit(): Flow<Habit?>
    suspend fun setAsWallpaperHabit(id: Long)
    suspend fun toggleCompletion(habitId: Long, date: LocalDate)
}
