package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.repository.HabitRepository

class SetWallpaperHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(id: Long) {
        repository.setAsWallpaperHabit(id)
    }
}
