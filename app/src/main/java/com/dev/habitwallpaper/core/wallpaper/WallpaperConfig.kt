package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Color

object WallpaperConfig {
    // Colors
    val BACKGROUND_GRADIENT_START = Color.parseColor("#F8F9FA")
    val BACKGROUND_GRADIENT_END = Color.parseColor("#E9ECEF")
    val TEXT_PRIMARY = Color.parseColor("#212529")
    val TEXT_SECONDARY = Color.parseColor("#6C757D")
    val STREAK_ORANGE = Color.parseColor("#FF9800")
    val GRID_EMPTY = Color.parseColor("#DEE2E6")
    val GRID_FILLED = Color.parseColor("#7CB342")

    // Typography
    const val TITLE_TEXT_SIZE_SP = 28f
    const val SUBTITLE_TEXT_SIZE_SP = 16f
    
    // Layout
    const val GRID_COLUMNS = 7
    const val GRID_ROWS = 4 // ~28 days
    const val GRID_CELL_SIZE_DP = 24f
    const val GRID_SPACING_DP = 6f
    const val VERTICAL_BIAS = 0.4f // Center content slightly above middle
}
