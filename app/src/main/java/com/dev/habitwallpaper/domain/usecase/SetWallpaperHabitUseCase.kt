package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.repository.HabitRepository
import javax.inject.Inject

class SetWallpaperHabitUseCase @Inject constructor(private val repository: HabitRepository) {
    suspend operator fun invoke(id: Long) {
        repository.setAsWallpaperHabit(id)
    }
}
