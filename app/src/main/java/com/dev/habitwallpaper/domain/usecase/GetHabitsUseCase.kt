package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class GetHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> {
        return repository.getAllHabits()
    }
}
