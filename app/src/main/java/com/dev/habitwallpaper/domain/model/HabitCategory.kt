package com.dev.habitwallpaper.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class HabitCategory(val displayName: String, val icon: ImageVector) {
    HEALTH("Health", Icons.Default.Favorite),
    FITNESS("Fitness", Icons.AutoMirrored.Filled.DirectionsRun),
    LEARNING("Learning", Icons.Default.Book),
    PRODUCTIVITY("Productivity", Icons.Default.Timer),
    MINDFULNESS("Mindfulness", Icons.Default.SelfImprovement),
    LIFESTYLE("Lifestyle", Icons.Default.WbSunny),
    PERSONAL_DEVELOPMENT("Development", Icons.AutoMirrored.Filled.TrendingUp),
    GENERAL("General", Icons.Default.Category)
}
