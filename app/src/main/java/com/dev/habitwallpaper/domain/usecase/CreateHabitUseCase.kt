package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import javax.inject.Inject

class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Long {
        return repository.insertHabit(habit)
    }
}
