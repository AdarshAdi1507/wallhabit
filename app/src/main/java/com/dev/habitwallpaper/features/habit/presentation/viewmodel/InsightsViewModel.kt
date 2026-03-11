package com.dev.habitwallpaper.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.usecase.GetHabitsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

/**
 * Holds all expensive-to-build lookup structures derived from a habits snapshot.
 * Computed once on [Dispatchers.Default] and reused across all [TimeRange] queries.
 *
 * Key optimisation: each habit's completion dates are stored in a [HashSet] so that
 * membership tests are O(1) instead of the O(n) cost of scanning a List.
 *
 * Additionally, [completionsPerDay] is a global map from date → count of habits
 * completed that day, and [activeHabitsPerDay] maps date → count of habits whose
 * challenge window covers that day. These two maps are built in a single O(H × C)
 * pass (H = habits, C = completions per habit) and allow any per-day metric to be
 * answered in O(1) afterwards.
 */
private data class HabitsIndex(
    /** Original list, preserved for the UI. */
    val habits: List<Habit>,

    /** Per-habit O(1) date lookup. Index aligned with [habits]. */
    val completionSets: List<HashSet<LocalDate>>,

    /**
     * Global aggregation maps keyed by [LocalDate].
     * Built in one linear pass — O(H × C) total.
     */
    val completionsPerDay: Map<LocalDate, Int>,
    val activeHabitsPerDay: Map<LocalDate, Int>,

    // ── Habit-level aggregations (range-independent) ──────────────────────
    val sortedByCompletionRate: List<Habit>,
    val longestStreakHabit: Habit?,
    val highestConsistencyHabit: Habit?,
    val totalCompletionsAllTime: Int
)

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class InsightsViewModel(
    private val getHabitsUseCase: GetHabitsUseCase
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)

    /**
     * Single reactive pipeline:
     *
     *  1. `getHabitsUseCase()` emits whenever the Room database changes.
     *  2. `combine` pairs the latest habits list with the selected range.
     *  3. `flowOn(Dispatchers.Default)` ensures the entire `combine` lambda —
     *     including index construction and range computation — runs on a background
     *     thread pool, never on the main thread.
     *  4. `stateIn` caches the last value so Compose recompositions receive it
     *     instantly without re-executing any computation.
     */
    val uiState: StateFlow<InsightsUiState> = combine(
        getHabitsUseCase(),
        _selectedRange
    ) { habits, range ->
        if (habits.isEmpty()) return@combine InsightsUiState(isLoading = false)

        // Step 1 — build the index (O(H × C))
        val index = buildIndex(habits)

        // Step 2 — derive range-specific metrics (O(D) where D = days in range)
        computeState(index, range)
    }
        .flowOn(Dispatchers.Default)   // ← background thread for all computation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState()
        )

    fun selectRange(range: TimeRange) {
        _selectedRange.value = range
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Step 1 — Index construction   O(H × C)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Builds all lookup structures from [habits] in a single linear pass.
     *
     * Complexity: **O(H × C)** where H = number of habits, C = average completions per habit.
     * This is the theoretical minimum — every completion record must be read at least once.
     *
     * After this function returns, every per-day or per-habit query is answered in O(1)
     * via hash-map or hash-set lookups instead of repeated linear scans.
     */
    private fun buildIndex(habits: List<Habit>): HabitsIndex {
        // Per-habit O(1) date lookup sets — O(C) per habit, O(H × C) total
        val completionSets = habits.map { h ->
            HashSet<LocalDate>(h.completions.size * 2).also { set ->
                h.completions.forEach { c -> set.add(c.date) }
            }
        }

        // Global per-day completion count — single pass over all completions
        // completionsPerDay[date] = number of habits completed on that date
        val completionsPerDay = HashMap<LocalDate, Int>()
        habits.forEach { h ->
            h.completions.forEach { c ->
                completionsPerDay[c.date] = (completionsPerDay[c.date] ?: 0) + 1
            }
        }

        // Habit-level aggregations — O(H)
        // completionRate and longestStreak are computed properties on Habit; we read each once.
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

    /**
     * Builds a dense per-day map of active habit counts using a linear sweep.
     *
     * Uses the sweep-line (difference array) technique:
     *  - emit +1 at each habit's startDate
     *  - emit -1 at each habit's (endDate + 1)
     *  - sort events once: O(H log H)
     *  - sweep through sorted dates accumulating the running total: O(D_span)
     *
     * Total: **O(H log H + D_span)** — far better than the naive O(H × D) scan.
     */
    private fun buildActivePerDayMap(habits: List<Habit>): Map<LocalDate, Int> {
        if (habits.isEmpty()) return emptyMap()

        // Collect boundary events
        val events = HashMap<LocalDate, Int>()
        habits.forEach { h ->
            events[h.startDate]              = (events[h.startDate]              ?: 0) + 1
            val after = h.endDate.plusDays(1)
            events[after] = (events[after] ?: 0) - 1
        }

        // Sweep — expand into a dense map only over the range we actually need
        val result = HashMap<LocalDate, Int>()
        var running = 0
        var cursor: LocalDate? = null
        events.keys.sorted().forEach { eventDate ->
            // Fill every date between last event and this one with the current running total
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

    // ─────────────────────────────────────────────────────────────────────
    //  Step 2 — Range-specific state derivation   O(D)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Derives [InsightsUiState] from the prebuilt [index] for the given [range].
     *
     * All per-day lookups are now **O(1)** hash-map gets, so the total cost for
     * a range of D days is **O(D)** — independent of habits count or completions count.
     *
     * For the Week range   D =  7  → 7 lookups
     * For the Month range  D = 30  → 30 lookups
     * For the All-Time range D ≤ 365 (typical) → ≤ 365 lookups
     */
    private fun computeState(index: HabitsIndex, range: TimeRange): InsightsUiState {
        val today = LocalDate.now()

        // Date window for selected period
        val (rangeStart, rangeEnd) = when (range) {
            TimeRange.WEEK     -> today.minusDays(6) to today
            TimeRange.MONTH    -> today.minusDays(29) to today
            TimeRange.ALL_TIME -> {
                val earliest = index.habits.minOfOrNull { it.startDate } ?: today.minusDays(90)
                earliest to today
            }
        }
        val daysInRange = ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1

        // Current period — O(D) lookups
        val (currentPossible, currentCompleted) = aggregatePeriod(
            index, rangeStart, daysInRange
        )
        val consistencyPercent = if (currentPossible > 0)
            (currentCompleted * 100 / currentPossible) else 0

        // Previous period — O(D) lookups
        val prevEnd   = rangeStart.minusDays(1)
        val prevStart = prevEnd.minusDays((daysInRange - 1).toLong())
        val (prevPossible, prevCompleted) = aggregatePeriod(index, prevStart, daysInRange)
        val previousConsistencyPercent = if (prevPossible > 0)
            (prevCompleted * 100 / prevPossible) else 0

        // Heatmap — O(D) lookups, one DayConsistency per day
        val heatmapDays = buildHeatmap(index, rangeStart, daysInRange)

        // Weekly bars — O(bars × 7) = O(const) lookups
        val weeklyBars = buildWeeklyBars(index, range, today)

        return InsightsUiState(
            habits                    = index.habits,
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

    // ─────────────────────────────────────────────────────────────────────
    //  Helpers — all O(D) using prebuilt maps
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Returns (totalPossible, totalCompleted) for [daysInRange] days starting at [start].
     * Each day is a single O(1) map lookup — total cost O(D).
     */
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

    /**
     * Builds the heatmap list. Each entry requires two O(1) map lookups.
     * Total: O(D).
     */
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

    /**
     * Builds bar-chart data. Each bar covers 7 days → 7 O(1) lookups each.
     * Total: O(bars × 7) = O(const) regardless of habits or completions count.
     */
    private fun buildWeeklyBars(
        index: HabitsIndex,
        range: TimeRange,
        today: LocalDate
    ): List<WeeklyBar> = when (range) {
        TimeRange.WEEK -> {
            // 7 bars — one per day
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
            // 4 bars — one per week
            (3 downTo 0).map { weekOffset ->
                val weekEnd   = today.minusDays((weekOffset * 7).toLong())
                val weekStart = weekEnd.minusDays(6)
                val (possible, completed) = aggregatePeriod(index, weekStart, 7)
                val rate = if (possible > 0) completed.toFloat() / possible else 0f
                WeeklyBar("W${4 - weekOffset}", rate)
            }
        }
        TimeRange.ALL_TIME -> {
            // 8 bars — one per week
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



