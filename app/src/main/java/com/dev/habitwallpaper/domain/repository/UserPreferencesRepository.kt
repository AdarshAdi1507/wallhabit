package com.dev.habitwallpaper.domain.repository

import com.dev.habitwallpaper.domain.model.WallpaperCustomization
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /** Emits the stored username, or null if onboarding has not been completed. */
    val userName: Flow<String?>

    /** Returns true if onboarding has been completed (name saved). */
    val isOnboardingCompleted: Flow<Boolean>

    /**
     * True once we have already asked the user for POST_NOTIFICATIONS permission.
     * Used to avoid showing the rationale dialog on every launch.
     */
    val notificationPermissionRequested: Flow<Boolean>

    /** Persists the user's name and marks onboarding as complete. */
    suspend fun saveUserName(name: String)

    /** Records that we have already prompted the user for the notification permission. */
    suspend fun markNotificationPermissionRequested()

    /** Emits the current wallpaper customization settings. */
    val wallpaperCustomization: Flow<WallpaperCustomization>

    /** Updates the wallpaper customization settings. */
    suspend fun updateWallpaperCustomization(customization: WallpaperCustomization)
}
