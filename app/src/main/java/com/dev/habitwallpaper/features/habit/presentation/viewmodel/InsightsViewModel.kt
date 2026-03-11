package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class TimeRange(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    ALL_TIME("All Time")
}

data class DayConsistency(val date: LocalDate, val completedCount: Int, val totalHabits: Int) {
    val ratio: Float get() = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f
}

data class WeeklyBar(val label: String, val completionRate: Float)

data class InsightsUiState(
    val habits: List<Habit> = emptyList(),
    val selectedRange: TimeRange = TimeRange.WEEK,
    val consistencyPercent: Int = 0,
    val previousConsistencyPercent: Int = 0,
    val heatmapDays: List<DayConsistency> = emptyList(),
    val sortedHabits: List<Habit> = emptyList(),
    val showAllHabits: Boolean = false,
    val weeklyBars: List<WeeklyBar> = emptyList(),
    val longestStreakHabit: Habit? = null,
    val highestConsistencyHabit: Habit? = null,
    val totalCompletionsAllTime: Int = 0,
    val isLoading: Boolean = true
)

class InsightsViewModel(
    private val getHabitsUseCase: GetHabitsUseCase
) : ViewModel() {

    private val _selectedRange = kotlinx.coroutines.flow.MutableStateFlow(TimeRange.WEEK)

    val uiState: StateFlow<InsightsUiState> = kotlinx.coroutines.flow.combine(
        getHabitsUseCase(),
        _selectedRange
    ) { habits, range ->
        computeState(habits, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    fun selectRange(range: TimeRange) {
        _selectedRange.value = range
    }

    private fun computeState(habits: List<Habit>, range: TimeRange): InsightsUiState {
        if (habits.isEmpty()) return InsightsUiState(isLoading = false)

        val today = LocalDate.now()

        // --- Date range for selected period ---
        val (rangeStart, rangeEnd) = when (range) {
            TimeRange.WEEK -> today.minusDays(6) to today
            TimeRange.MONTH -> today.minusDays(29) to today
            TimeRange.ALL_TIME -> {
                val earliest = habits.minOfOrNull { it.startDate } ?: today.minusDays(90)
                earliest to today
            }
        }
        val daysInRange = ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1

        // --- Current period consistency ---
        val currentDays = (0 until daysInRange).map { rangeStart.plusDays(it.toLong()) }
        val totalPossible = currentDays.sumOf { date ->
            habits.count { h -> !date.isBefore(h.startDate) && !date.isAfter(h.endDate) }
        }
        val totalCompleted = currentDays.sumOf { date ->
            habits.count { h -> h.completedDates.contains(date) }
        }
        val consistencyPercent = if (totalPossible > 0) (totalCompleted * 100 / totalPossible) else 0

        // --- Previous period consistency ---
        val prevRangeEnd = rangeStart.minusDays(1)
        val prevRangeStart = prevRangeEnd.minusDays((daysInRange - 1).toLong())
        val prevDays = (0 until daysInRange).map { prevRangeStart.plusDays(it.toLong()) }
        val prevPossible = prevDays.sumOf { date ->
            habits.count { h -> !date.isBefore(h.startDate) && !date.isAfter(h.endDate) }
        }
        val prevCompleted = prevDays.sumOf { date ->
            habits.count { h -> h.completedDates.contains(date) }
        }
        val previousConsistencyPercent = if (prevPossible > 0) (prevCompleted * 100 / prevPossible) else 0

        // --- Heatmap: aggregate daily consistency ---
        val heatmapDays = currentDays.map { date ->
            val activeHabits = habits.count { h -> !date.isBefore(h.startDate) && !date.isAfter(h.endDate) }
            val completed = habits.count { h -> h.completedDates.contains(date) }
            DayConsistency(date, completed, activeHabits)
        }

        // --- Habit performance sorted by completion rate ---
        val sortedHabits = habits.sortedByDescending { it.completionRate }

        // --- Weekly bars (last 8 weeks or months) ---
        val weeklyBars = buildWeeklyBars(habits, range, today)

        // --- Achievements ---
        val longestStreakHabit = habits.maxByOrNull { it.longestStreak }
        val highestConsistencyHabit = habits.maxByOrNull { it.completionRate }
        val totalCompletionsAllTime = habits.sumOf { it.totalCompleted }

        return InsightsUiState(
            habits = habits,
            selectedRange = range,
            consistencyPercent = consistencyPercent,
            previousConsistencyPercent = previousConsistencyPercent,
            heatmapDays = heatmapDays,
            sortedHabits = sortedHabits,
            weeklyBars = weeklyBars,
            longestStreakHabit = longestStreakHabit,
            highestConsistencyHabit = highestConsistencyHabit,
            totalCompletionsAllTime = totalCompletionsAllTime,
            isLoading = false
        )
    }

    private fun buildWeeklyBars(habits: List<Habit>, range: TimeRange, today: LocalDate): List<WeeklyBar> {
        return when (range) {
            TimeRange.WEEK -> {
                // Last 7 days — one bar per day
                (6 downTo 0).map { offset ->
                    val date = today.minusDays(offset.toLong())
                    val activeHabits = habits.count { h -> !date.isBefore(h.startDate) && !date.isAfter(h.endDate) }
                    val completed = habits.count { h -> h.completedDates.contains(date) }
                    val rate = if (activeHabits > 0) completed.toFloat() / activeHabits else 0f
                    val label = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    WeeklyBar(label, rate)
                }
            }
            TimeRange.MONTH -> {
                // Last 4 weeks — one bar per week
                (3 downTo 0).map { weekOffset ->
                    val weekEnd = today.minusDays((weekOffset * 7).toLong())
                    val weekStart = weekEnd.minusDays(6)
                    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
                    val possible = days.sumOf { d -> habits.count { h -> !d.isBefore(h.startDate) && !d.isAfter(h.endDate) } }
                    val completed = days.sumOf { d -> habits.count { h -> h.completedDates.contains(d) } }
                    val rate = if (possible > 0) completed.toFloat() / possible else 0f
                    val label = "W${4 - weekOffset}"
                    WeeklyBar(label, rate)
                }
            }
            TimeRange.ALL_TIME -> {
                // Last 8 weeks
                (7 downTo 0).map { weekOffset ->
                    val weekEnd = today.minusDays((weekOffset * 7).toLong())
                    val weekStart = weekEnd.minusDays(6)
                    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
                    val possible = days.sumOf { d -> habits.count { h -> !d.isBefore(h.startDate) && !d.isAfter(h.endDate) } }
                    val completed = days.sumOf { d -> habits.count { h -> h.completedDates.contains(d) } }
                    val rate = if (possible > 0) completed.toFloat() / possible else 0f
                    val label = "W${8 - weekOffset}"
                    WeeklyBar(label, rate)
                }
            }
        }
    }
}



