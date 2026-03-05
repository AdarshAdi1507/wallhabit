package com.dev.habitwallpaper.core.wallpaper

import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.HabitTypography
import com.dev.habitwallpaper.core.designsystem.toAndroid

object WallpaperConfig {
    // Colors derived from Centralized Design System
    val BACKGROUND_GRADIENT_START = HabitColors.MINT_SURFACE.toAndroid()
    val BACKGROUND_GRADIENT_END = HabitColors.GRID_EMPTY.toAndroid()
    val TEXT_PRIMARY = HabitColors.ON_SURFACE_TEXT.toAndroid()
    val TEXT_SECONDARY = HabitColors.SECONDARY_TEXT.toAndroid()
    val STREAK_ORANGE = HabitColors.STREAK_ORANGE.toAndroid()
    val GRID_EMPTY = HabitColors.GRID_EMPTY.toAndroid()
    val GRID_FILLED = HabitColors.GRID_HIGH.toAndroid()

    // Typography derived from Centralized Design System
    const val TITLE_TEXT_SIZE_SP = HabitTypography.TITLE_SIZE_SP
    const val SUBTITLE_TEXT_SIZE_SP = HabitTypography.SUBTITLE_SIZE_SP
    
    // Layout
    const val GRID_COLUMNS = 7
    const val GRID_ROWS = 4 // ~28 days
    const val GRID_CELL_SIZE_DP = 24f
    const val GRID_SPACING_DP = 6f
    const val VERTICAL_BIAS = 0.4f // Center content slightly above middle
}
