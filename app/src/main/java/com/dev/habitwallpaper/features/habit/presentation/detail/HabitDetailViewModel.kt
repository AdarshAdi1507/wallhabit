package com.dev.habitwallpaper.features.habit.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitUseCase
import com.dev.habitwallpaper.domain.usecase.SetWallpaperHabitUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitDetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true
)

class HabitDetailViewModel(
    private val habitId: Long,
    private val getHabitUseCase: GetHabitUseCase,
    private val setWallpaperHabitUseCase: SetWallpaperHabitUseCase,
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        loadHabit()
    }

    private fun loadHabit() {
        getHabitUseCase(habitId)
            .onEach { habit ->
                _uiState.update { it.copy(habit = habit, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleCompletion() {
        val currentHabit = _uiState.value.habit ?: return
        viewModelScope.launch {
            repository.toggleCompletion(currentHabit.id, LocalDate.now())
        }
    }

    fun setAsWallpaper() {
        viewModelScope.launch {
            setWallpaperHabitUseCase(habitId)
        }
    }
}
