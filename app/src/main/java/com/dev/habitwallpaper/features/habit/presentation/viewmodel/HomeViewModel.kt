package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import android.content.Context
import android.content.Intent
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
    private val repository: HabitRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getHabitsUseCase()
        .map { habits ->
            val totalCount = habits.size
            val completedCount = habits.count { it.isCompletedToday }
            
            // Priority habit is the one selected as wallpaper
            val wallpaperHabit = habits.find { it.isWallpaperSelected }
            
            // Focus habit: use priority habit if available, otherwise follow previous logic
            val focusHabit = wallpaperHabit ?: habits.firstOrNull { !it.isCompletedToday } ?: habits.firstOrNull()
            
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

    fun toggleHabitCompletion(habit: Habit, value: Float = 1f) {
        viewModelScope.launch {
            repository.toggleCompletion(habit.id, LocalDate.now(), value)
            
            // If this is the wallpaper habit, trigger an update
            if (habit.isWallpaperSelected) {
                triggerWallpaperUpdate()
            }
        }
    }

    private fun triggerWallpaperUpdate() {
        val intent = Intent("com.dev.habitwallpaper.UPDATE_WALLPAPER")
        context.sendBroadcast(intent)
    }
}
