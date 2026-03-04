package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class GetHabitUseCase(private val repository: HabitRepository) {
    operator fun invoke(id: Long): Flow<Habit?> {
        return repository.getHabitById(id)
    }
}
