package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val habits: List<Habit> = emptyList(),
    val searchQuery: String = "",
    val filter: HabitFilter = HabitFilter.ALL,
    val isLoading: Boolean = true
)

enum class HabitFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    PAUSED("Paused")
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val repository: HabitRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(HabitFilter.ALL)
    val filter: StateFlow<HabitFilter> = _filter.asStateFlow()

    val uiState: StateFlow<HabitsUiState> = combine(
        getHabitsUseCase(),
        _searchQuery,
        _filter
    ) { habits, query, filter ->
        val filteredHabits = habits.filter { habit ->
            val matchesQuery = habit.name.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                HabitFilter.ALL -> true
                HabitFilter.ACTIVE -> !habit.isPaused && !habit.isCompleted
                HabitFilter.COMPLETED -> habit.isCompleted
                HabitFilter.PAUSED -> habit.isPaused
            }
            matchesQuery && matchesFilter
        }
        HabitsUiState(
            habits = filteredHabits,
            searchQuery = query,
            filter = filter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: HabitFilter) {
        _filter.value = filter
    }

    fun togglePauseHabit(habit: Habit) {
        viewModelScope.launch {
            repository.pauseHabit(habit.id, !habit.isPaused)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun setAsPriority(habitId: Long) {
        viewModelScope.launch {
            repository.setAsWallpaperHabit(habitId)
        }
    }
}
