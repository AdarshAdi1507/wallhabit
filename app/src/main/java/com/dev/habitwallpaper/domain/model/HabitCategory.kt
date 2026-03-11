package com.dev.habitwallpaper.domain.model

/**
 * Pure domain enum representing the category of a [Habit].
 *
 * This enum contains only business-logic identifiers and carries NO UI dependencies
 * (no Compose, no Android resources, no display strings). It is intentionally
 * framework-agnostic so the domain layer remains independently testable.
 *
 * UI concerns such as human-readable labels and icons are handled exclusively in
 * the presentation layer via extension properties defined in:
 *   `com.dev.habitwallpaper.features.habit.presentation.util.HabitCategoryExt`
 */
enum class HabitCategory {
    HEALTH,
    FITNESS,
    LEARNING,
    PRODUCTIVITY,
    MINDFULNESS,
    LIFESTYLE,
    PERSONAL_DEVELOPMENT,
    GENERAL
}
