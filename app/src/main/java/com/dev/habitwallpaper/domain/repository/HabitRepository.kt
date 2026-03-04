package com.dev.habitwallpaper.domain.repository

import com.dev.habitwallpaper.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun insertHabit(habit: Habit)
    fun getAllHabits(): Flow<List<Habit>>
}
