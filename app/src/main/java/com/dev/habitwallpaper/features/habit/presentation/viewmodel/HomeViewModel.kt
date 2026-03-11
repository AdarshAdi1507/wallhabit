package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.core.wallpaper.WallpaperManager
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.repository.UserPreferencesRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val focusHabit: Habit? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val wallpaperHabit: Habit? = null,
    val weeklyConsistency: Float = 0f,
    val isLoading: Boolean = true,
    val userName: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val repository: HabitRepository,
    private val wallpaperManager: WallpaperManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getHabitsUseCase(),
        userPreferencesRepository.userName
    ) { habits, userName ->
        val totalCount = habits.size
        val completedCount = habits.count { it.isCompletedToday }

        val wallpaperHabit = habits.find { it.isWallpaperSelected }
        val focusHabit = wallpaperHabit
            ?: habits.firstOrNull { !it.isCompletedToday }
            ?: habits.firstOrNull()

        val today = LocalDate.now()
        val last7Days = (0..6).map { today.minusDays(it.toLong()) }
        val totalCheckinsPossible = habits.size * 7
        val actualCheckins = habits.sumOf { habit ->
            last7Days.count { date -> habit.completedDates.contains(date) }
        }
        val weeklyConsistency = if (totalCheckinsPossible > 0) {
            actualCheckins.toFloat() / totalCheckinsPossible
        } else 0f

        HomeUiState(
            habits = habits,
            focusHabit = focusHabit,
            completedCount = completedCount,
            totalCount = totalCount,
            wallpaperHabit = wallpaperHabit,
            weeklyConsistency = weeklyConsistency,
            isLoading = false,
            userName = userName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleHabitCompletion(habit: Habit, value: Float = 1f) {
        viewModelScope.launch {
            repository.toggleCompletion(habit.id, LocalDate.now(), value)
            if (habit.isWallpaperSelected) {
                wallpaperManager.triggerUpdate()
            }
        }
    }
}


