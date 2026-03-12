package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.repository.HabitRepository
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
//  Public data types (unchanged API — InsightsScreen reads these)
// ─────────────────────────────────────────────────────────────────────────────

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
    val allAchievements: List<Achievement> = emptyList(),
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

// ─────────────────────────────────────────────────────────────────────────────
//  Internal precomputed index — built once per habits snapshot
// ─────────────────────────────────────────────────────────────────────────────

private data class HabitsIndex(
    val habits: List<Habit>,
    val completionSets: List<HashSet<LocalDate>>,
    val completionsPerDay: Map<LocalDate, Int>,
    val activeHabitsPerDay: Map<LocalDate, Int>,
    val sortedByCompletionRate: List<Habit>,
    val longestStreakHabit: Habit?,
    val highestConsistencyHabit: Habit?,
    val totalCompletionsAllTime: Int
)

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())

    init {
        // Since we want all achievements across all habits for a general insights view
        // We'll observe all habits and for each habit, fetch its achievements
        // A more efficient way would be a dedicated repository method to get all achievements
        // But for now, let's collect them from all habits
        getHabitsUseCase().onEach { habits ->
            val allAchievements = mutableListOf<Achievement>()
            habits.forEach { habit ->
                habitRepository.getAchievementsForHabit(habit.id).first().let {
                    allAchievements.addAll(it)
                }
            }
            _achievements.value = allAchievements.sortedByDescending { it.achievedDate }
        }.launchIn(viewModelScope)
    }

    val uiState: StateFlow<InsightsUiState> = combine(
        getHabitsUseCase(),
        _selectedRange,
        _achievements
    ) { habits, range, achievements ->
        if (habits.isEmpty()) return@combine InsightsUiState(isLoading = false, allAchievements = achievements)

        val index = buildIndex(habits)
        computeState(index, range, achievements)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState()
        )

    fun selectRange(range: TimeRange) {
        _selectedRange.value = range
    }

    private fun buildIndex(habits: List<Habit>): HabitsIndex {
        val completionSets = habits.map { h ->
            HashSet<LocalDate>(h.completions.size * 2).also { set ->
                h.completions.forEach { c -> set.add(c.date) }
            }
        }

        val completionsPerDay = HashMap<LocalDate, Int>()
        habits.forEach { h ->
            h.completions.forEach { c ->
                completionsPerDay[c.date] = (completionsPerDay[c.date] ?: 0) + 1
            }
        }

        val sortedByCompletionRate  = habits.sortedByDescending { it.completionRate }
        val longestStreakHabit      = habits.maxByOrNull { it.longestStreak }
        val highestConsistencyHabit = habits.maxByOrNull { it.completionRate }
        val totalCompletionsAllTime = habits.sumOf { it.totalCompleted }

        return HabitsIndex(
            habits                  = habits,
            completionSets          = completionSets,
            completionsPerDay       = completionsPerDay,
            activeHabitsPerDay      = buildActivePerDayMap(habits),
            sortedByCompletionRate  = sortedByCompletionRate,
            longestStreakHabit      = longestStreakHabit,
            highestConsistencyHabit = highestConsistencyHabit,
            totalCompletionsAllTime = totalCompletionsAllTime
        )
    }

    private fun buildActivePerDayMap(habits: List<Habit>): Map<LocalDate, Int> {
        if (habits.isEmpty()) return emptyMap()

        val events = HashMap<LocalDate, Int>()
        habits.forEach { h ->
            events[h.startDate]              = (events[h.startDate]              ?: 0) + 1
            val after = h.endDate.plusDays(1)
            events[after] = (events[after] ?: 0) - 1
        }

        val result = HashMap<LocalDate, Int>()
        var running = 0
        var cursor: LocalDate? = null
        events.keys.sorted().forEach { eventDate ->
            val from = cursor
            if (from != null && running > 0) {
                var d: LocalDate = from
                while (d.isBefore(eventDate)) {
                    result[d] = running
                    d = d.plusDays(1)
                }
            }
            running += events[eventDate]!!
            cursor = eventDate
        }
        return result
    }

    private fun computeState(index: HabitsIndex, range: TimeRange, achievements: List<Achievement>): InsightsUiState {
        val today = LocalDate.now()

        val (rangeStart, rangeEnd) = when (range) {
            TimeRange.WEEK     -> today.minusDays(6) to today
            TimeRange.MONTH    -> today.minusDays(29) to today
            TimeRange.ALL_TIME -> {
                val earliest = index.habits.minOfOrNull { it.startDate } ?: today.minusDays(90)
                earliest to today
            }
        }
        val daysInRange = ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1

        val (currentPossible, currentCompleted) = aggregatePeriod(
            index, rangeStart, daysInRange
        )
        val consistencyPercent = if (currentPossible > 0)
            (currentCompleted * 100 / currentPossible) else 0

        val prevEnd   = rangeStart.minusDays(1)
        val prevStart = prevEnd.minusDays((daysInRange - 1).toLong())
        val (prevPossible, prevCompleted) = aggregatePeriod(index, prevStart, daysInRange)
        val previousConsistencyPercent = if (prevPossible > 0)
            (prevCompleted * 100 / prevPossible) else 0

        val heatmapDays = buildHeatmap(index, rangeStart, daysInRange)
        val weeklyBars = buildWeeklyBars(index, range, today)

        return InsightsUiState(
            habits                    = index.habits,
            allAchievements           = achievements,
            selectedRange             = range,
            consistencyPercent        = consistencyPercent,
            previousConsistencyPercent = previousConsistencyPercent,
            heatmapDays               = heatmapDays,
            sortedHabits              = index.sortedByCompletionRate,
            weeklyBars                = weeklyBars,
            longestStreakHabit        = index.longestStreakHabit,
            highestConsistencyHabit   = index.highestConsistencyHabit,
            totalCompletionsAllTime   = index.totalCompletionsAllTime,
            isLoading                 = false
        )
    }

    private fun aggregatePeriod(
        index: HabitsIndex,
        start: LocalDate,
        daysInRange: Int
    ): Pair<Int, Int> {
        var possible  = 0
        var completed = 0
        repeat(daysInRange) { offset ->
            val date = start.plusDays(offset.toLong())
            possible  += index.activeHabitsPerDay[date] ?: 0
            completed += index.completionsPerDay[date]  ?: 0
        }
        return possible to completed
    }

    private fun buildHeatmap(
        index: HabitsIndex,
        start: LocalDate,
        daysInRange: Int
    ): List<DayConsistency> = List(daysInRange) { offset ->
        val date      = start.plusDays(offset.toLong())
        val active    = index.activeHabitsPerDay[date]  ?: 0
        val completed = index.completionsPerDay[date]   ?: 0
        DayConsistency(date, completed, active)
    }

    private fun buildWeeklyBars(
        index: HabitsIndex,
        range: TimeRange,
        today: LocalDate
    ): List<WeeklyBar> = when (range) {
        TimeRange.WEEK -> {
            (6 downTo 0).map { offset ->
                val date   = today.minusDays(offset.toLong())
                val active = index.activeHabitsPerDay[date] ?: 0
                val done   = index.completionsPerDay[date]  ?: 0
                val rate   = if (active > 0) done.toFloat() / active else 0f
                val label  = date.dayOfWeek.name.take(3)
                    .lowercase().replaceFirstChar { it.uppercase() }
                WeeklyBar(label, rate)
            }
        }
        TimeRange.MONTH -> {
            (3 downTo 0).map { weekOffset ->
                val weekEnd   = today.minusDays((weekOffset * 7).toLong())
                val weekStart = weekEnd.minusDays(6)
                val (possible, completed) = aggregatePeriod(index, weekStart, 7)
                val rate = if (possible > 0) completed.toFloat() / possible else 0f
                WeeklyBar("W${4 - weekOffset}", rate)
            }
        }
        TimeRange.ALL_TIME -> {
            (7 downTo 0).map { weekOffset ->
                val weekEnd   = today.minusDays((weekOffset * 7).toLong())
                val weekStart = weekEnd.minusDays(6)
                val (possible, completed) = aggregatePeriod(index, weekStart, 7)
                val rate = if (possible > 0) completed.toFloat() / possible else 0f
                WeeklyBar("W${8 - weekOffset}", rate)
            }
        }
    }
}
