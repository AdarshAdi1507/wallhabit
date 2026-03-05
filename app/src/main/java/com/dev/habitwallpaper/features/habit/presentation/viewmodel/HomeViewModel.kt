package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val focusHabit: Habit? = null,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val wallpaperHabit: Habit? = null,
    val weeklyConsistency: Float = 0f,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getHabitsUseCase()
        .map { habits ->
            val totalCount = habits.size
            val completedCount = habits.count { it.isCompletedToday }
            
            // Focus habit: first uncompleted habit, or first habit if all completed
            val focusHabit = habits.firstOrNull { !it.isCompletedToday } ?: habits.firstOrNull()
            
            val wallpaperHabit = habits.find { it.isWallpaperSelected }
            
            // Weekly consistency (last 7 days)
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
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            repository.toggleCompletion(habit.id, LocalDate.now())
        }
    }
}
