package com.dev.habitwallpaper.core.designsystem

import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Centralized Design System for WallHabit.
 *
 * Theme Philosophy: Calm, layered green reflecting growth, discipline, and clarity.
 * Uses deep forest greens for primary actions and soft emerald tones for highlights.
 * Surfaces are mint-tinted to reduce eye strain and provide a premium feel.
 */
object HabitColors {
    // --- Brand Colors (Forest & Emerald) ---
    const val FOREST_DEEP = 0xFF1B4332   // Deep Forest Green (Primary)
    const val EMERALD_SOFT = 0xFF52B788  // Softer Emerald (Highlights)
    const val MINT_SURFACE = 0xFFF7FFF7  // Subtle Mint-tinted Background
    
    // --- Typography & UI ---
    const val ON_SURFACE_TEXT = 0xFF2F3E46  // Dark Gray (Softer than black)
    const val SECONDARY_TEXT = 0xFF52796F   // Muted Sage for details
    
    // --- Heatmap / Grid Intensity Gradient ---
    const val GRID_EMPTY = 0xFFE9F5EB    // Very light pastel green (Missed/Empty)
    const val GRID_LOW = 0xFFB7E4C7      // Low consistency
    const val GRID_MEDIUM = 0xFF74C69D   // Medium consistency
    const val GRID_HIGH = 0xFF2D6A4F     // High consistency (Rich Forest)

    // --- Dark Mode Palette ---
    const val DARK_GREEN_BLACK = 0xFF081C15 // Deep green-black base
    const val GLOW_EMERALD = 0xFF74C69D     // Glowing emerald accents

    // --- Functional Mapping (Light) ---
    const val PRIMARY = FOREST_DEEP
    const val SECONDARY = EMERALD_SOFT
    const val BACKGROUND = MINT_SURFACE
    const val SURFACE = MINT_SURFACE
    
    const val STREAK_ORANGE = 0xFFD68C45  // Muted sunset orange for streaks
}

/**
 * Helper extensions to convert raw hex Longs to platform-specific Color objects.
 */
fun Long.toCompose() = ComposeColor(this)
fun Long.toAndroid() = (this and 0xFFFFFFFF).toInt()

object HabitTypography {
    const val TITLE_SIZE_SP = 28f
    const val SUBTITLE_SIZE_SP = 16f
    const val BODY_SIZE_SP = 14f
}
