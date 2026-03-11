package com.dev.habitwallpaper.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /** Emits the stored username, or null if onboarding has not been completed. */
    val userName: Flow<String?>

    /** Returns true if onboarding has been completed (name saved). */
    val isOnboardingCompleted: Flow<Boolean>

    /** Persists the user's name and marks onboarding as complete. */
    suspend fun saveUserName(name: String)
}

