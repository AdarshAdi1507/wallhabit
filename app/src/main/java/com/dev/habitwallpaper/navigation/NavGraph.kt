package com.dev.habitwallpaper.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dev.habitwallpaper.domain.usecase.*
import com.dev.habitwallpaper.features.habit.presentation.screen.HabitSetupScreen
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeViewModel
import com.dev.habitwallpaper.features.habit.presentation.detail.HabitDetailScreen
import com.dev.habitwallpaper.features.habit.presentation.detail.HabitDetailViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev.habitwallpaper.core.notification.AlarmScheduler
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.features.habit.presentation.screen.HomeScreen

sealed class Screen(val route: String) {
    object HabitSetup : Screen("habit_setup")
    object Home : Screen("home")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: HabitRepository
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HabitViewModelFactory(repository, context)
            )
            HomeScreen(
                viewModel = viewModel,
                onAddHabit = {
                    navController.navigate(Screen.HabitSetup.route)
                },
                onHabitClick = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }
        composable(Screen.HabitSetup.route) {
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModelFactory(repository, context)
            )
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
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            val viewModel: HabitDetailViewModel = viewModel(
                factory = HabitDetailViewModelFactory(habitId, repository)
            )
            HabitDetailScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HabitViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HabitViewModel(
                    CreateHabitUseCase(repository),
                    AlarmScheduler(context),
                    context
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(
                    GetHabitsUseCase(repository),
                    repository,
                    context
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class HabitDetailViewModelFactory(
    private val habitId: Long,
    private val repository: HabitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitDetailViewModel(
                habitId, 
                GetHabitUseCase(repository), 
                SetWallpaperHabitUseCase(repository),
                repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
