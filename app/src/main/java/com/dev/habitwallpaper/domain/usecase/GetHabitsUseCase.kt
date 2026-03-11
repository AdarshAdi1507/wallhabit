package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitsUseCase @Inject constructor(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> {
        return repository.getAllHabits()
    }
}
