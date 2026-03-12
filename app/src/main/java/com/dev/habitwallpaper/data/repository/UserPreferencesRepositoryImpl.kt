package com.dev.habitwallpaper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.dev.habitwallpaper.domain.model.*
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
        
        // Wallpaper Customization Keys
        val THEME_TYPE = stringPreferencesKey("wp_theme_type")
        val BACKGROUND_TYPE = stringPreferencesKey("wp_background_type")
        val GRID_SHAPE = stringPreferencesKey("wp_grid_shape")
        val COMPLETED_CELL_COLOR = intPreferencesKey("wp_completed_cell_color")
        val EMPTY_CELL_COLOR = intPreferencesKey("wp_empty_cell_color")
        val PALETTE_NAME = stringPreferencesKey("wp_palette_name")
        val PROGRESS_STYLE = stringPreferencesKey("wp_progress_style")
        val GRID_LAYOUT_TYPE = stringPreferencesKey("wp_grid_layout_type")
        val GRID_SPACING = floatPreferencesKey("wp_grid_spacing")
        val GRID_POSITION = stringPreferencesKey("wp_grid_position")
        val BG_COLOR_START = intPreferencesKey("wp_bg_color_start")
        val BG_COLOR_END = intPreferencesKey("wp_bg_color_end")
        val SHOW_GLOW = booleanPreferencesKey("wp_show_glow")
        val SHOW_SHADOW = booleanPreferencesKey("wp_show_shadow")
        val HIGHLIGHT_TODAY = booleanPreferencesKey("wp_highlight_today")
        val PULSE_EFFECT = booleanPreferencesKey("wp_pulse_effect")
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

    override val wallpaperCustomization: Flow<WallpaperCustomization> = context.userPreferencesDataStore.data
        .map { prefs ->
            WallpaperCustomization(
                themeType = prefs[Keys.THEME_TYPE] ?: "Default",
                backgroundType = try {
                    BackgroundType.valueOf(prefs[Keys.BACKGROUND_TYPE] ?: BackgroundType.GRADIENT.name)
                } catch (e: Exception) { BackgroundType.GRADIENT },
                gridShape = try {
                    GridShape.valueOf(prefs[Keys.GRID_SHAPE] ?: GridShape.ROUNDED_SQUARE.name)
                } catch (e: Exception) { GridShape.ROUNDED_SQUARE },
                completedCellColor = prefs[Keys.COMPLETED_CELL_COLOR] ?: WallpaperCustomization.Default.completedCellColor,
                emptyCellColor = prefs[Keys.EMPTY_CELL_COLOR] ?: WallpaperCustomization.Default.emptyCellColor,
                paletteName = prefs[Keys.PALETTE_NAME] ?: "Matcha Green",
                progressStyle = try {
                    ProgressStyle.valueOf(prefs[Keys.PROGRESS_STYLE] ?: ProgressStyle.SOLID.name)
                } catch (e: Exception) { ProgressStyle.SOLID },
                gridLayoutType = try {
                    GridLayoutType.valueOf(prefs[Keys.GRID_LAYOUT_TYPE] ?: GridLayoutType.STANDARD.name)
                } catch (e: Exception) { GridLayoutType.STANDARD },
                gridSpacing = prefs[Keys.GRID_SPACING] ?: WallpaperCustomization.Default.gridSpacing,
                gridPosition = try {
                    GridPosition.valueOf(prefs[Keys.GRID_POSITION] ?: GridPosition.CENTER.name)
                } catch (e: Exception) { GridPosition.CENTER },
                backgroundColorStart = prefs[Keys.BG_COLOR_START] ?: WallpaperCustomization.Default.backgroundColorStart,
                backgroundColorEnd = prefs[Keys.BG_COLOR_END] ?: WallpaperCustomization.Default.backgroundColorEnd,
                showGlow = prefs[Keys.SHOW_GLOW] ?: false,
                showShadow = prefs[Keys.SHOW_SHADOW] ?: false,
                highlightToday = prefs[Keys.HIGHLIGHT_TODAY] ?: false,
                pulseEffect = prefs[Keys.PULSE_EFFECT] ?: false
            )
        }

    override suspend fun updateWallpaperCustomization(customization: WallpaperCustomization) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.THEME_TYPE] = customization.themeType
            prefs[Keys.BACKGROUND_TYPE] = customization.backgroundType.name
            prefs[Keys.GRID_SHAPE] = customization.gridShape.name
            prefs[Keys.COMPLETED_CELL_COLOR] = customization.completedCellColor
            prefs[Keys.EMPTY_CELL_COLOR] = customization.emptyCellColor
            prefs[Keys.PALETTE_NAME] = customization.paletteName
            prefs[Keys.PROGRESS_STYLE] = customization.progressStyle.name
            prefs[Keys.GRID_LAYOUT_TYPE] = customization.gridLayoutType.name
            prefs[Keys.GRID_SPACING] = customization.gridSpacing
            prefs[Keys.GRID_POSITION] = customization.gridPosition.name
            prefs[Keys.BG_COLOR_START] = customization.backgroundColorStart
            prefs[Keys.BG_COLOR_END] = customization.backgroundColorEnd
            prefs[Keys.SHOW_GLOW] = customization.showGlow
            prefs[Keys.SHOW_SHADOW] = customization.showShadow
            prefs[Keys.HIGHLIGHT_TODAY] = customization.highlightToday
            prefs[Keys.PULSE_EFFECT] = customization.pulseEffect
        }
    }
}
