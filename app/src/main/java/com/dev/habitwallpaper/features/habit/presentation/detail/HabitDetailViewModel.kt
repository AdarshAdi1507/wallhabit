package com.dev.habitwallpaper.features.habit.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.Milestone
import com.dev.habitwallpaper.domain.model.MilestoneThresholds
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitUseCase
import com.dev.habitwallpaper.domain.usecase.SetWallpaperHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitDetailUiState(
    val habit: Habit? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val nextMilestone: Milestone? = null,
    val daysToNextMilestone: Int? = null
)

sealed class HabitDetailEvent {
    data class AchievementReached(val achievement: Achievement) : HabitDetailEvent()
}

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getHabitUseCase: GetHabitUseCase,
    private val setWallpaperHabitUseCase: SetWallpaperHabitUseCase,
    private val repository: HabitRepository
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle["habitId"])

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<HabitDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadHabit()
        loadAchievements()
    }

    private fun loadHabit() {
        getHabitUseCase(habitId)
            .onEach { habit ->
                habit?.let { h ->
                    val next = MilestoneThresholds.getNextMilestone(h.currentStreak)
                    val daysRemaining = next?.let { it.value - h.currentStreak }
                    _uiState.update { it.copy(
                        habit = h, 
                        isLoading = false,
                        nextMilestone = next,
                        daysToNextMilestone = daysRemaining
                    ) }
                } ?: _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadAchievements() {
        repository.getAchievementsForHabit(habitId)
            .onEach { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleCompletion(date: LocalDate = LocalDate.now()) {
        val currentHabit = _uiState.value.habit ?: return
        viewModelScope.launch {
            val achievement = repository.toggleCompletion(currentHabit.id, date)
            if (achievement != null) {
                _eventFlow.emit(HabitDetailEvent.AchievementReached(achievement))
            }
        }
    }

    fun setAsWallpaper() {
        viewModelScope.launch {
            setWallpaperHabitUseCase(habitId)
        }
    }
}
