package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import com.dev.habitwallpaper.domain.usecase.SetWallpaperHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperSelectionUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WallpaperSelectionViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val setWallpaperHabitUseCase: SetWallpaperHabitUseCase
) : ViewModel() {

    val uiState: StateFlow<WallpaperSelectionUiState> = getHabitsUseCase()
        .map { habits ->
            WallpaperSelectionUiState(
                habits = habits,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WallpaperSelectionUiState()
        )

    fun selectWallpaperHabit(habitId: Long) {
        viewModelScope.launch {
            setWallpaperHabitUseCase(habitId)
        }
    }
}
