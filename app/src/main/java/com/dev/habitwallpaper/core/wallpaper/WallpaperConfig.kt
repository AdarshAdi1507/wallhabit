package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Color

object WallpaperConfig {
    // --- Colors (Apple iOS Clean/Minimal Aesthetic) ---
    val BACKGROUND_GRADIENT_START = Color.parseColor("#E8F1E9") // Soft pale sage green
    val BACKGROUND_GRADIENT_END = Color.parseColor("#E1ECEB")   // Mist blue-green
    
    val APP_LABEL_COLOR = Color.parseColor("#8E8E93")          // Muted sage gray (iOS-like)
    val HABIT_NAME_COLOR = Color.parseColor("#1C1C1E")         // Dark charcoal (iOS-like)
    val STREAK_TEXT_COLOR = Color.parseColor("#3A3A3C")        // Muted dark color
    
    val GRID_COMPLETED = Color.parseColor("#58D68D")           // Soft vibrant mint green
    val GRID_INCOMPLETE_BORDER = Color.parseColor("#A3B18A")    // Subtle sage green border
    val GRID_INCOMPLETE_BG = Color.parseColor("#1AFFFFFF")     // Very light translucent background (10% white)

    val RAINBOW_COLORS = listOf(
        Color.parseColor("#FFADAD"), // Pastel Red
        Color.parseColor("#FFD6A5"), // Pastel Orange
        Color.parseColor("#FDFFB6"), // Pastel Yellow
        Color.parseColor("#CAFFBF"), // Pastel Green
        Color.parseColor("#9BF6FF"), // Pastel Blue
        Color.parseColor("#A0C4FF"), // Pastel Periwinkle
        Color.parseColor("#BDB2FF"), // Pastel Purple
        Color.parseColor("#FFC6FF")  // Pastel Pink
    )

    // --- Typography (DM Sans Style) ---
    const val APP_LABEL_SIZE_SP = 12f
    const val HABIT_NAME_SIZE_SP = 48f
    const val STREAK_TEXT_SIZE_SP = 14f
    
    const val APP_LABEL_LETTER_SPACING = 0.1f
    const val HABIT_NAME_LETTER_SPACING = -0.05f // Tight premium feel (-0.5 in 1000-unit is -0.05 in Canvas letterSpacing)
    const val STREAK_TEXT_LETTER_SPACING = 0.2f  // Wide (2.0 in 1000-unit is 0.2 in Canvas)

    // --- Layout ---
    const val GRID_COLUMNS = 7
    const val GRID_ROWS = 3
    const val GRID_SPACING_DP = 10f
    
    const val GRID_CENTER_Y_PERCENT = 0.65f // Lower-middle section
    const val GRID_SIDE_MARGIN_PERCENT = 0.15f
}
