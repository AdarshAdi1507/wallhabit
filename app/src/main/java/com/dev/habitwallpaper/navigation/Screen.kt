package com.dev.habitwallpaper.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Habits : Screen("habits")
    object Insights : Screen("insights")
    object Wallpaper : Screen("wallpaper")
    object Profile : Screen("profile")
    
    object HabitSetup : Screen("habit_setup")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object WallpaperSelection : Screen("wallpaper_selection")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    object Habits : BottomNavItem(Screen.Habits.route, "Habits", Icons.AutoMirrored.Filled.List)
    object Insights : BottomNavItem(Screen.Insights.route, "Insights", Icons.Default.BarChart)
    object Wallpaper : BottomNavItem(Screen.Wallpaper.route, "Wallpaper", Icons.Default.Wallpaper)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)

    companion object {
        val items = listOf(Home, Habits, Insights, Wallpaper, Profile)
    }
}
