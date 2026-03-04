package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class ObserveWallpaperHabitUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<Habit?> {
        return repository.getWallpaperHabit()
    }
}
