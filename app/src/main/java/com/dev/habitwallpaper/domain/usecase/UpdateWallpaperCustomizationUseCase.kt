package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.WallpaperCustomization
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class UpdateWallpaperCustomizationUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(customization: WallpaperCustomization) {
        repository.updateWallpaperCustomization(customization)
    }
}
