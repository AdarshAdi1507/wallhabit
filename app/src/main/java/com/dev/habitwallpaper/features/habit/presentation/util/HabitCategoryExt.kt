package com.dev.habitwallpaper.features.habit.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.habitwallpaper.domain.model.HabitCategory

/**
 * Presentation-layer mappings for [HabitCategory].
 *
 * The domain enum intentionally contains no UI concerns (no display strings, no icons).
 * All presentation-specific mappings live here as Kotlin extension properties so that:
 *  - The domain layer stays pure Kotlin with zero Android/Compose dependencies.
 *  - UI representations are co-located in the presentation layer, easy to update.
 *  - The mapping is exhaustive (when expressions) so new categories cause a compile error
 *    if a UI mapping is forgotten.
 */

/**
 * Human-readable display label for this category, suitable for use in UI widgets.
 * Kept in the presentation layer so the domain enum carries no localisation strings.
 */
val HabitCategory.displayName: String
    get() = when (this) {
        HabitCategory.HEALTH -> "Health"
        HabitCategory.FITNESS -> "Fitness"
        HabitCategory.LEARNING -> "Learning"
        HabitCategory.PRODUCTIVITY -> "Productivity"
        HabitCategory.MINDFULNESS -> "Mindfulness"
        HabitCategory.LIFESTYLE -> "Lifestyle"
        HabitCategory.PERSONAL_DEVELOPMENT -> "Development"
        HabitCategory.GENERAL -> "General"
    }

/**
 * Compose [ImageVector] icon associated with this category.
 * Kept in the presentation layer to avoid importing Compose into the domain.
 */
val HabitCategory.icon: ImageVector
    get() = when (this) {
        HabitCategory.HEALTH -> Icons.Default.Favorite
        HabitCategory.FITNESS -> Icons.AutoMirrored.Filled.DirectionsRun
        HabitCategory.LEARNING -> Icons.Default.Book
        HabitCategory.PRODUCTIVITY -> Icons.Default.Timer
        HabitCategory.MINDFULNESS -> Icons.Default.SelfImprovement
        HabitCategory.LIFESTYLE -> Icons.Default.WbSunny
        HabitCategory.PERSONAL_DEVELOPMENT -> Icons.AutoMirrored.Filled.TrendingUp
        HabitCategory.GENERAL -> Icons.Default.Category
    }
