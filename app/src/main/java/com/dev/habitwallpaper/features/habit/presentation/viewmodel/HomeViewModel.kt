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
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val repository: HabitRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getHabitsUseCase()
        .map { habits -> 
            HomeUiState(habits = habits, isLoading = false) 
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
