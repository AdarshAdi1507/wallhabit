package com.dev.habitwallpaper.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dev.habitwallpaper.domain.usecase.CreateHabitUseCase
import com.dev.habitwallpaper.features.habit.presentation.screen.HabitSetupScreen
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.features.habit.presentation.screen.HomeScreen

sealed class Screen(val route: String) {
    object HabitSetup : Screen("habit_setup")
    object Home : Screen("home")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: HabitRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HabitSetup.route
    ) {
        composable(Screen.HabitSetup.route) {
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModelFactory(CreateHabitUseCase(repository))
            )
            HabitSetupScreen(
                viewModel = viewModel,
                onHabitCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.HabitSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}

class HabitViewModelFactory(
    private val createHabitUseCase: CreateHabitUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(createHabitUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
