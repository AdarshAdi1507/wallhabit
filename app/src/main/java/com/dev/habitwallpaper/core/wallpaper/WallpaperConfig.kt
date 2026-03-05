package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Color
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.HabitTypography
import com.dev.habitwallpaper.core.designsystem.toAndroid

object WallpaperConfig {
    // Colors derived from Centralized Design System
    val BACKGROUND_GRADIENT_START = HabitColors.MINT_SURFACE.toAndroid()
    val BACKGROUND_GRADIENT_END = Color.parseColor("#E0E0E0")
    val TEXT_PRIMARY = HabitColors.ON_SURFACE_TEXT.toAndroid()
    val TEXT_SECONDARY = HabitColors.SECONDARY_TEXT.toAndroid()
    val STREAK_ORANGE = HabitColors.STREAK_ORANGE.toAndroid()
    
    val GRID_NEUTRAL = Color.parseColor("#F5F5F5")
    val GRID_EMPTY = HabitColors.GRID_EMPTY.toAndroid()
    val GRID_FILLED = HabitColors.GRID_HIGH.toAndroid()

    // Rainbow Theme Colors for Artwork
    val RAINBOW_COLORS = listOf(
        Color.parseColor("#FFADAD"), // Red
        Color.parseColor("#FFD6A5"), // Orange
        Color.parseColor("#FDFFB6"), // Yellow
        Color.parseColor("#CAFFBF"), // Green
        Color.parseColor("#9BF6FF"), // Blue
        Color.parseColor("#A0C4FF"), // Indigo
        Color.parseColor("#BDB2FF"), // Violet
        Color.parseColor("#FFC6FF")  // Pink
    )

    // Typography
    const val TITLE_TEXT_SIZE_SP = 32f
    const val SUBTITLE_TEXT_SIZE_SP = 16f
    
    // Layout - The Artwork Grid
    const val GRID_COLUMNS = 15 // Wider for the artwork look
    const val GRID_SPACING_DP = 4f
    
    // Artwork Area (percent of screen)
    const val GRID_TOP_MARGIN_PERCENT = 0.35f
    const val GRID_BOTTOM_MARGIN_PERCENT = 0.10f
    const val GRID_SIDE_MARGIN_PERCENT = 0.08f

    /**
     * Calculates a theme color for a specific cell index to create a diagonal pattern.
     */
    fun getThemeColor(index: Int, columns: Int): Int {
        val row = index / columns
        val col = index % columns
        // Diagonal pattern: sum of row and col determines color
        val colorIndex = (row + col) % RAINBOW_COLORS.size
        return RAINBOW_COLORS[colorIndex]
    }
}
