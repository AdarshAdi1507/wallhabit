package com.dev.habitwallpaper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dev.habitwallpaper.features.habit.presentation.detail.HabitDetailScreen
import com.dev.habitwallpaper.features.habit.presentation.detail.HabitDetailViewModel
import com.dev.habitwallpaper.features.habit.presentation.screen.HabitSetupScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.HabitsScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.HomeScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.InsightsScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.OnboardingScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.WallpaperSelectionScreen
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitsViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.InsightsViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.OnboardingViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WallpaperSelectionViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onAddHabit = {
                    navController.navigate(Screen.HabitSetup.route)
                },
                onHabitClick = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onWallpaperClick = {
                    navController.navigate(Screen.Wallpaper.route)
                }
            )
        }

        composable(Screen.Habits.route) {
            val viewModel: HabitsViewModel = hiltViewModel()
            HabitsScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate(Screen.HabitSetup.route) },
                onHabitClick = { habitId -> navController.navigate(Screen.HabitDetail.createRoute(habitId)) },
                onEditHabit = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(Screen.Insights.route) {
            val viewModel: InsightsViewModel = hiltViewModel()
            InsightsScreen(viewModel = viewModel)
        }

        composable(Screen.Wallpaper.route) {
            val viewModel: WallpaperSelectionViewModel = hiltViewModel()
            WallpaperSelectionScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            // Placeholder for Profile
        }

        composable(Screen.HabitSetup.route) {
            val viewModel: HabitViewModel = hiltViewModel()
            HabitSetupScreen(
                viewModel = viewModel,
                onHabitCreated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) {
            val viewModel: HabitDetailViewModel = hiltViewModel()
            HabitDetailScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
