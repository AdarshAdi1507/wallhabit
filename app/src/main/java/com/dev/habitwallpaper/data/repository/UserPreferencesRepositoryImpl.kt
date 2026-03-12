package com.dev.habitwallpaper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATION_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    }

    override val userName: Flow<String?> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[Keys.USER_NAME] }

    override val isOnboardingCompleted: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    override val notificationPermissionRequested: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[Keys.NOTIFICATION_PERMISSION_REQUESTED] ?: false }

    override suspend fun saveUserName(name: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.trim()
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun markNotificationPermissionRequested() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_PERMISSION_REQUESTED] = true
        }
    }
}

