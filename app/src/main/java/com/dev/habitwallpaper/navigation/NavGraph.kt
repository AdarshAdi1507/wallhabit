package com.dev.habitwallpaper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.features.habit.presentation.screen.HomeScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.HabitsScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.InsightsScreen
import com.dev.habitwallpaper.features.habit.presentation.screen.WallpaperSelectionScreen
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitsViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.InsightsViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WallpaperSelectionViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: HabitRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
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
                },
                onWallpaperClick = {
                    navController.navigate(Screen.Wallpaper.route)
                }
            )
        }
        
        composable(Screen.Habits.route) {
            val viewModel: HabitsViewModel = viewModel(
                factory = HabitViewModelFactory(repository, context)
            )
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
            val viewModel: InsightsViewModel = viewModel(
                factory = InsightsViewModelFactory(repository)
            )
            InsightsScreen(viewModel = viewModel)
        }

        composable(Screen.Wallpaper.route) {
            val viewModel: WallpaperSelectionViewModel = viewModel(
                factory = WallpaperSelectionViewModelFactory(repository)
            )
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
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Important: Use applicationContext to avoid leaking Activity context
        val appContext = context.applicationContext
        val wallpaperManager = WallpaperManager(appContext)
        val alarmScheduler = AlarmScheduler(appContext)

        return when {
            modelClass.isAssignableFrom(HabitViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HabitViewModel(
                    CreateHabitUseCase(repository),
                    alarmScheduler,
                    wallpaperManager
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HomeViewModel(
                    GetHabitsUseCase(repository),
                    repository,
                    wallpaperManager
                ) as T
            }
            modelClass.isAssignableFrom(HabitsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HabitsViewModel(
                    GetHabitsUseCase(repository),
                    repository
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

class WallpaperSelectionViewModelFactory(
    private val repository: HabitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallpaperSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallpaperSelectionViewModel(
                GetHabitsUseCase(repository),
                SetWallpaperHabitUseCase(repository)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class InsightsViewModelFactory(
    private val repository: HabitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(GetHabitsUseCase(repository)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
