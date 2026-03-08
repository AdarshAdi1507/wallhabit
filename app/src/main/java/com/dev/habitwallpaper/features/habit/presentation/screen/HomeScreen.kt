package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.TrackingType
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onWallpaperClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var habitForNumericInput by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = onAddHabit) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Habit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.habits.isEmpty()) {
            EmptyState(onAddHabit)
        } else {
            HomeContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onToggle = { habit ->
                    if (habit.trackingType == TrackingType.NUMERIC && !habit.isCompletedToday) {
                        habitForNumericInput = habit
                    } else {
                        viewModel.toggleHabitCompletion(habit)
                    }
                },
                onHabitClick = onHabitClick,
                onWallpaperClick = onWallpaperClick
            )
        }
    }

    if (habitForNumericInput != null) {
        NumericCompletionDialog(
            habit = habitForNumericInput!!,
            onDismiss = { habitForNumericInput = null },
            onConfirm = { value ->
                viewModel.toggleHabitCompletion(habitForNumericInput!!, value)
                habitForNumericInput = null
            }
        )
    }
}

@Composable
fun NumericCompletionDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var value by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Progress") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter the value for \"${habit.name}\"")
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.isEmpty() || it.toFloatOrNull() != null) value = it },
                    placeholder = { Text("Target: ${habit.goalValue}") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value.toFloatOrNull() ?: 1f) },
                enabled = value.isNotEmpty()
            ) {
                Text("Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    innerPadding: PaddingValues,
    onToggle: (Habit) -> Unit,
    onHabitClick: (Long) -> Unit,
    onWallpaperClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Focus Habit Section
        uiState.focusHabit?.let { habit ->
            item {
                FocusHabitCard(habit = habit, onToggle = { onToggle(habit) })
            }
        }

        // 2. Daily Progress Indicator
        item {
            DailyProgressSection(
                completed = uiState.completedCount,
                total = uiState.totalCount
            )
        }

        // 3. Habit Cards Section
        item {
            Text(
                "Your Habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(uiState.habits) { habit ->
            HabitCardV2(
                habit = habit,
                onToggle = { onToggle(habit) },
                onClick = { onHabitClick(habit.id) }
            )
        }

        // 4. Wallpaper Preview Section
        item {
            WallpaperPreviewSection(uiState.wallpaperHabit, onWallpaperClick)
        }

        // 5. Consistency Insights
        item {
            ConsistencyInsights(uiState)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FocusHabitCard(habit: Habit, onToggle: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "FocusCardBackground"
    )
    val contentColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TODAY'S FOCUS",
                style = MaterialTheme.typography.labelLarge,
                color = if (habit.isCompletedToday) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                habit.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = if (habit.isCompletedToday) contentColor else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${habit.currentStreak} Day Streak",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (habit.isCompletedToday) contentColor else Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (!habit.isCompletedToday) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Complete Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = contentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crushed it!", fontWeight = FontWeight.Bold, color = contentColor)
                }
            }
        }
    }
}

@Composable
fun DailyProgressSection(completed: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Daily Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$completed of $total habits completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        val progress = if (total > 0) completed.toFloat() / total else 0f
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            Canvas(modifier = Modifier.size(64.dp)) {
                drawCircle(color = Color(0xFFE0E0E0), style = Stroke(width = 8.dp.toPx()))
                drawArc(
                    color = Color(0xFF56AB2F), // Keeping a vibrant green for progress
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HabitCardV2(habit: Habit, onToggle: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = habit.color?.let { Color(it).copy(alpha = 0.2f) } ?: MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = habit.category.icon,
                            contentDescription = null,
                            tint = habit.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            habit.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${habit.currentStreak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = HabitColors.STREAK_ORANGE.toCompose(),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        if (habit.isCompletedToday) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "Complete",
                        tint = if (habit.isCompletedToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HabitConsistencyPreview(habit)
        }
    }
}

@Composable
fun HabitConsistencyPreview(habit: Habit) {
    val today = LocalDate.now()
    val last7Days = (0..6).reversed().map { today.minusDays(it.toLong()) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Last 7 days",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            last7Days.forEach { date ->
                val isBeforeStart = date.isBefore(habit.startDate)
                val isCompleted = habit.completedDates.contains(date)
                
                val boxColor = when {
                    isBeforeStart -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    isCompleted -> habit.color?.let { Color(it) } ?: HabitColors.GRID_HIGH.toCompose()
                    else -> HabitColors.GRID_EMPTY.toCompose()
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            color = boxColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun WallpaperPreviewSection(wallpaperHabit: Habit?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Fixed-size Square Preview
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (wallpaperHabit != null) {
                    MiniWallpaperPreview(
                        habit = wallpaperHabit,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Wallpaper,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Center: Vertical Text Stack
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Wallpaper Habit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = wallpaperHabit?.name ?: "No habit selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (wallpaperHabit != null) {
                    Text(
                        text = "Day ${wallpaperHabit.currentDay} of ${wallpaperHabit.durationDays}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Right: Navigation Indicator
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ConsistencyInsights(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Weekly Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${(uiState.weeklyConsistency * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "consistent this week",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val topHabit = uiState.habits.maxByOrNull { it.currentStreak }
            if (topHabit != null && topHabit.currentStreak > 0) {
                Text(
                    "🔥 Your longest streak is ${topHabit.name} – ${topHabit.currentStreak} days",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EmptyState(onAddHabit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Default.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Start your journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Create a habit and set it as your wallpaper to stay focused.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddHabit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create First Habit")
            }
        }
    }
}
