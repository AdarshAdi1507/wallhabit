package com.dev.habitwallpaper.domain.usecase

import com.dev.habitwallpaper.domain.model.WallpaperCustomization
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWallpaperCustomizationUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<WallpaperCustomization> = repository.wallpaperCustomization
}
