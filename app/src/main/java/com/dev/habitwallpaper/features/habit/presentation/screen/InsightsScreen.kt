package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.features.habit.presentation.util.icon
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.DayConsistency
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.InsightsUiState
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.InsightsViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.TimeRange
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WeeklyBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

// ─────────────────────────────────────────────────────────────────────────────
//  Root screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showAllHabits by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.habits.isEmpty()) {
            InsightsEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            InsightsDashboard(
                uiState = uiState,
                innerPadding = innerPadding,
                showAllHabits = showAllHabits,
                onRangeSelected = { viewModel.selectRange(it) },
                onToggleShowAll = { showAllHabits = !showAllHabits }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Dashboard scroll body
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsightsDashboard(
    uiState: InsightsUiState,
    innerPadding: PaddingValues,
    showAllHabits: Boolean,
    onRangeSelected: (TimeRange) -> Unit,
    onToggleShowAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { TimeRangeSelector(selected = uiState.selectedRange, onSelect = onRangeSelected) }
        item {
            ConsistencySummaryCard(
                consistencyPercent = uiState.consistencyPercent,
                previousConsistencyPercent = uiState.previousConsistencyPercent,
                range = uiState.selectedRange
            )
        }
        item { InsightsSectionTitle("Consistency Heatmap") }
        item { ConsistencyHeatmap(days = uiState.heatmapDays, range = uiState.selectedRange) }
        
        if (uiState.allAchievements.isNotEmpty()) {
            item { InsightsSectionTitle("Recent Milestones") }
            item { RecentAchievementsRow(uiState.allAchievements) }
        }

        item { InsightsSectionTitle("Habit Performance") }
        val habitsToShow = if (showAllHabits) uiState.sortedHabits else uiState.sortedHabits.take(3)
        items(habitsToShow, key = { it.id }) { habit -> HabitPerformanceRow(habit = habit) }
        if (uiState.sortedHabits.size > 3) {
            item {
                TextButton(onClick = onToggleShowAll, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (showAllHabits) "Show Less" else "View All ${uiState.sortedHabits.size} Habits",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item { InsightsSectionTitle("Progress Trends") }
        item { ProgressTrendsChart(bars = uiState.weeklyBars, range = uiState.selectedRange) }
        item { InsightsSectionTitle("Milestones & Highlights") }
        item { AchievementsSection(uiState = uiState) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  1. Time-range selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimeRangeSelector(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(250), label = "rangeTab"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(250), label = "rangeTabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onSelect(range) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    range.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  2. Consistency summary card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConsistencySummaryCard(
    consistencyPercent: Int,
    previousConsistencyPercent: Int,
    range: TimeRange
) {
    val delta = consistencyPercent - previousConsistencyPercent
    val trendColor = when {
        delta > 0 -> Color(0xFF2D6A4F)
        delta < 0 -> Color(0xFFE57373)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val trendIcon = when {
        delta > 0 -> Icons.AutoMirrored.Filled.TrendingUp
        delta < 0 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    val trendLabel = when {
        delta > 0 -> "+${delta}% vs previous period"
        delta < 0 -> "${delta}% vs previous period"
        else -> "Same as previous period"
    }
    val contextMessage = when {
        consistencyPercent >= 80 -> "Excellent work! Keep it up 🎉"
        consistencyPercent >= 60 -> "Good progress. Stay consistent!"
        consistencyPercent >= 40 -> "Building momentum. Push through!"
        consistencyPercent > 0  -> "Every day counts. Start small!"
        else -> "No completions yet in this period."
    }
    val animatedPercent by animateFloatAsState(
        targetValue = consistencyPercent.toFloat(), animationSpec = tween(800), label = "consistencyAnim"
    )
    val progressAnim by animateFloatAsState(
        targetValue = consistencyPercent / 100f, animationSpec = tween(900), label = "progressBar"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = when (range) {
                    TimeRange.WEEK -> "This Week's Consistency"
                    TimeRange.MONTH -> "This Month's Consistency"
                    TimeRange.ALL_TIME -> "All-Time Consistency"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "${animatedPercent.toInt()}%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text(
                        contextMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(trendLabel, style = MaterialTheme.typography.labelSmall, color = trendColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  3. Consistency Heatmap
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConsistencyHeatmap(days: List<DayConsistency>, range: TimeRange) {
    val emptyColor = HabitColors.GRID_EMPTY.toCompose()
    val lowColor   = HabitColors.GRID_LOW.toCompose()
    val medColor   = HabitColors.GRID_MEDIUM.toCompose()
    val highColor  = HabitColors.GRID_HIGH.toCompose()

    fun intensityColor(ratio: Float): Color = when {
        ratio <= 0f    -> emptyColor
        ratio <= 0.33f -> lowColor
        ratio <= 0.66f -> medColor
        else           -> highColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily activity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    listOf(emptyColor, lowColor, medColor, highColor).forEach { color ->
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                    }
                    Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            when (range) {
                TimeRange.WEEK -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            days.forEach { day ->
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(intensityColor(day.ratio)))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            days.forEach { day ->
                                Text(
                                    day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
                TimeRange.MONTH -> HeatmapGrid(days = days, cellsPerRow = 7, gap = 6, intensityColor = ::intensityColor)
                TimeRange.ALL_TIME -> HeatmapGrid(days = days, cellsPerRow = 10, gap = 4, intensityColor = ::intensityColor)
            }
        }
    }
}

@Composable
private fun HeatmapGrid(
    days: List<DayConsistency>,
    cellsPerRow: Int,
    gap: Int,
    intensityColor: (Float) -> Color
) {
    val rows = ceil(days.size.toFloat() / cellsPerRow).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(gap.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
                for (col in 0 until cellsPerRow) {
                    val idx = row * cellsPerRow + col
                    if (idx < days.size) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(4.dp)).background(intensityColor(days[idx].ratio)))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAchievementsRow(achievements: List<Achievement>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(achievements.take(10)) { achievement ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥 ${achievement.milestoneValue}", fontWeight = FontWeight.Bold)
                    Text(
                        achievement.milestoneTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        achievement.achievedDate.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  4. Habit Performance Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HabitPerformanceRow(habit: Habit) {
    val completionPct = (habit.completionRate * 100).toInt()
    val barColor = habit.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    val animatedRate by animateFloatAsState(
        targetValue = habit.completionRate, animationSpec = tween(700), label = "habitPerfBar"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(barColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = habit.category.icon, contentDescription = null, tint = barColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(HabitColors.STREAK_ORANGE), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${habit.currentStreak}d", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(HabitColors.STREAK_ORANGE))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { animatedRate },
                        modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(50)),
                        color = barColor,
                        trackColor = barColor.copy(alpha = 0.12f)
                    )
                    Text("$completionPct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.width(32.dp))
                }
                MiniActivityGrid(habit = habit)
            }
        }
    }
}

@Composable
private fun MiniActivityGrid(habit: Habit) {
    val today = LocalDate.now()
    val last7 = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val barColor = habit.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        last7.forEach { date ->
            val isBefore = date.isBefore(habit.startDate)
            val isCompleted = habit.completedDates.contains(date)
            val cellColor = when {
                isBefore    -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                isCompleted -> barColor
                else        -> barColor.copy(alpha = 0.15f)
            }
            Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(cellColor))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  5. Progress Trends — bar chart via Canvas
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Owns exactly ONE [animateFloatAsState] for a single chart bar and writes the
 * current animated value into [rates][index].
 *
 * Why a separate composable?
 * Composable calls must be stable in count and order between recompositions.
 * Calling [animateFloatAsState] inside a `bars.map {}` loop violates this rule:
 * when the time range switches (e.g. WEEK→MONTH the count goes 7→4), Compose would
 * see a different number of animation-state slots and produce a runtime crash or
 * silently misassign animation values.
 *
 * By extracting each bar into its own composable and wrapping it with [key], each
 * bar occupies a fixed, labelled slot in the composition tree regardless of list
 * length changes.
 */
@Composable
private fun AnimatedBarValue(
    targetRate: Float,
    index: Int,
    rates: FloatArray,
    onUpdate: () -> Unit
) {
    val animated by animateFloatAsState(
        targetValue = targetRate,
        animationSpec = tween(durationMillis = 800),
        label = "bar_$index"
    )
    // Write into the shared array so the Canvas (a non-composable lambda) can read it.
    rates[index] = animated
    // Notify the parent that a new frame value is ready; parent re-draws the Canvas.
    onUpdate()
}

@Composable
private fun ProgressTrendsChart(bars: List<WeeklyBar>, range: TimeRange) {
    if (bars.isEmpty()) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    // Allocated once per bar-count change; AnimatedBarValue fills it each frame.
    val animatedRates = remember(bars.size) { FloatArray(bars.size) }

    // Incrementing this causes only the Canvas lambda to re-read animatedRates[],
    // not the entire composable tree, keeping recomposition cost minimal.
    var drawTick by remember { mutableIntStateOf(0) }

    // One composable per bar — each owns an independent, stable animation slot.
    // key(bar.label) guarantees correct slot reuse/reset when bars are replaced
    // (e.g. day labels Mon–Sun ↔ week labels W1–W4) between time range switches.
    bars.forEachIndexed { i, bar ->
        key(bar.label) {
            AnimatedBarValue(
                targetRate = bar.completionRate,
                index      = i,
                rates      = animatedRates,
                onUpdate   = { drawTick++ }
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = when (range) {
                    TimeRange.WEEK     -> "Daily completion this week"
                    TimeRange.MONTH    -> "Weekly completion this month"
                    TimeRange.ALL_TIME -> "Weekly completion (last 8 weeks)"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // drawTick is read here so this Canvas lambda re-executes on every
            // animation frame tick, painting the latest values from animatedRates[].
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
            ) {
                @Suppress("UNUSED_EXPRESSION") drawTick // establish read dependency
                val barCount    = bars.size
                val totalWidth  = size.width
                val chartHeight = size.height - 24.dp.toPx()
                val barWidth    = (totalWidth / barCount) * 0.55f
                val barSpacing  = totalWidth / barCount

                for (pct in listOf(0.25f, 0.5f, 0.75f, 1f)) {
                    val y = chartHeight * (1f - pct)
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(0f, y), end = Offset(totalWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                bars.forEachIndexed { i, _ ->
                    val centerX = barSpacing * i + barSpacing / 2f
                    val barLeft = centerX - barWidth / 2f
                    val barH    = (animatedRates[i] * chartHeight).coerceAtLeast(4.dp.toPx())
                    drawRoundRect(
                        color        = surfaceVariantColor,
                        topLeft      = Offset(barLeft, 0f),
                        size         = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawRoundRect(
                        color        = primaryColor,
                        topLeft      = Offset(barLeft, chartHeight - barH),
                        size         = Size(barWidth, barH),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                bars.forEach { bar ->
                    Text(
                        bar.label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  6. Achievements & Milestones
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AchievementsSection(uiState: InsightsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementTile(
                modifier = Modifier.weight(1f), emoji = "🔥", label = "Longest Streak",
                value = uiState.longestStreakHabit?.let { "${it.longestStreak} days\n${it.name}" } ?: "No streaks yet",
                color = Color(0xFFFF9800)
            )
            AchievementTile(
                modifier = Modifier.weight(1f), emoji = "⚡", label = "Total Check-ins",
                value = "${uiState.totalCompletionsAllTime} all time",
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementTile(
                modifier = Modifier.weight(1f), emoji = "🏆", label = "Top Habit",
                value = uiState.highestConsistencyHabit?.let { "${(it.completionRate * 100).toInt()}%\n${it.name}" } ?: "—",
                color = Color(0xFF56AB2F)
            )
            NextMilestoneTile(modifier = Modifier.weight(1f), habits = uiState.sortedHabits)
        }
    }
}

@Composable
private fun AchievementTile(modifier: Modifier = Modifier, emoji: String, label: String, value: String, color: Color) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun NextMilestoneTile(modifier: Modifier = Modifier, habits: List<Habit>) {
    val milestones = listOf(7, 14, 21, 30, 60, 90)
    val bestHabit = habits.minByOrNull { h ->
        (milestones.firstOrNull { it > h.currentStreak } ?: 100) - h.currentStreak
    }
    val nextMilestone = bestHabit?.let { h -> milestones.firstOrNull { it > h.currentStreak } }
    val daysLeft = if (bestHabit != null && nextMilestone != null) nextMilestone - bestHabit.currentStreak else null

    Card(
        modifier = modifier, shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🎯", style = MaterialTheme.typography.headlineSmall)
            Text("Next Milestone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            if (bestHabit != null && daysLeft != null) {
                Text(
                    "$daysLeft day${if (daysLeft != 1) "s" else ""} to\n${nextMilestone}d streak",
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text("Start a habit\nto track streaks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsightsSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun InsightsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(88.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No data yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Create and complete habits to see\nyour analytics here.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
