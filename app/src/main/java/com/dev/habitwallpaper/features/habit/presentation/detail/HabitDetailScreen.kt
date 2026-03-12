package com.dev.habitwallpaper.features.habit.presentation.detail

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.core.wallpaper.LiveWallpaperService
import com.dev.habitwallpaper.domain.model.Achievement
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.Milestone
import com.dev.habitwallpaper.domain.model.TrackingType
import com.dev.habitwallpaper.features.habit.presentation.util.displayName
import com.dev.habitwallpaper.features.habit.presentation.util.icon
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCelebration by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is HabitDetailEvent.AchievementReached) {
                showCelebration = event.achievement
                delay(2500)
                showCelebration = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.habit?.let { habit ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item { 
                            HabitHeader(
                                habit = habit,
                                onTogglePriority = { viewModel.setAsWallpaper() },
                                onEdit = { /* TODO: Navigate to Edit */ }
                            ) 
                        }
                        item { ProgressOverview(habit) }
                        
                        uiState.nextMilestone?.let { milestone ->
                            item { 
                                MilestoneProgress(
                                    currentStreak = habit.currentStreak,
                                    nextMilestone = milestone,
                                    daysRemaining = uiState.daysToNextMilestone ?: 0
                                )
                            }
                        }

                        item { GoalInformation(habit) }
                        item { 
                            ConsistencyMap(
                                habit = habit,
                                onToggleDate = { date -> viewModel.toggleCompletion(date) }
                            ) 
                        }
                        item { HabitStatistics(habit) }
                        
                        if (uiState.achievements.isNotEmpty()) {
                            item { AchievementHistory(uiState.achievements) }
                        }

                        if (habit.trackingType == TrackingType.NUMERIC) {
                            item { RecentLoggedValues(habit) }
                        }
                        
                        item {
                            TextButton(
                                onClick = { /* TODO: Pause Habit */ },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                            ) {
                                Icon(Icons.Default.PauseCircleOutline, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pause Habit Tracking")
                            }
                        }
                    }

                    // Bottom Actions
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        shadowElevation = 12.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                            putExtra(
                                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                                ComponentName(context, LiveWallpaperService::class.java)
                                            )
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    enabled = habit.isWallpaperSelected,
                                    border = BorderStroke(1.5.dp, HabitColors.FOREST_DEEP.toCompose().copy(alpha = if (habit.isWallpaperSelected) 0.3f else 0.1f))
                                ) {
                                    Icon(Icons.Default.Wallpaper, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Preview", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.toggleCompletion() },
                                    modifier = Modifier.weight(1.3f).height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (habit.isCompletedToday) Color.LightGray else HabitColors.FOREST_DEEP.toCompose()
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (habit.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.Add,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (habit.isCompletedToday) "Completed" else "Mark Done",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Celebration Overlay
                    AnimatedVisibility(
                        visible = showCelebration != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 1.2f)
                    ) {
                        showCelebration?.let { achievement ->
                            CelebrationCard(achievement)
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Habit not found")
            }
        }
    }
}

@Composable
fun MilestoneProgress(
    currentStreak: Int,
    nextMilestone: Milestone,
    daysRemaining: Int
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Next Milestone")
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = nextMilestone.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$daysRemaining days remaining until ${nextMilestone.value} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                val progress = if (nextMilestone.value > 0) {
                    currentStreak.toFloat() / nextMilestone.value.toFloat()
                } else 0f
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = HabitColors.STREAK_ORANGE.toCompose(),
                    trackColor = Color(0xFFF0F0F0)
                )
            }
        }
    }
}

@Composable
fun CelebrationCard(achievement: Achievement) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "🎉 Milestone Reached!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HabitColors.FOREST_DEEP.toCompose()
                )
                
                Surface(
                    shape = CircleShape,
                    color = HabitColors.STREAK_ORANGE.toCompose().copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "🔥",
                            fontSize = 48.sp
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${achievement.milestoneValue} Day Streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = achievement.milestoneTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                
                Text(
                    "You're making incredible progress! Keep going.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AchievementHistory(achievements: List<Achievement>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Milestone History")
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(achievements) { achievement ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
}

@Composable
fun HabitHeader(
    habit: Habit,
    onTogglePriority: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = CircleShape,
                color = HabitColors.EMERALD_SOFT.toCompose().copy(alpha = 0.15f),
                modifier = Modifier.size(90.dp).align(Alignment.Center)
            ) {
                Icon(
                    imageVector = habit.category.icon,
                    contentDescription = null,
                    tint = HabitColors.FOREST_DEEP.toCompose(),
                    modifier = Modifier.padding(24.dp)
                )
            }
            
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onTogglePriority) {
                    Icon(
                        imageVector = if (habit.isWallpaperSelected) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Set Priority",
                        tint = if (habit.isWallpaperSelected) Color(0xFFFFD700) else Color.Gray
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(HabitColors.ON_SURFACE_TEXT)
        )
        Text(
            text = habit.category.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = Color(HabitColors.SECONDARY_TEXT)
        )
    }
}

@Composable
fun ProgressOverview(habit: Habit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                val progress by animateFloatAsState(targetValue = habit.progress, label = "progress")
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFF0F0F0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = HabitColors.EMERALD_SOFT.toCompose(),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(habit.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressItem(Icons.Default.Whatshot, "Current Streak", "${habit.currentStreak} Days", HabitColors.STREAK_ORANGE.toCompose())
                ProgressItem(Icons.Default.CalendarToday, "Challenge", "Day ${habit.currentDay} of ${habit.durationDays}", Color(0xFF4A90E2))
                ProgressItem(
                    if (habit.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    "Today",
                    if (habit.isCompletedToday) "Completed" else "Pending",
                    if (habit.isCompletedToday) HabitColors.FOREST_DEEP.toCompose() else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProgressItem(icon: ImageVector, label: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GoalInformation(habit: Habit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Goal Timeline")
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBox("Goal Duration", "${habit.durationDays} Days", Modifier.weight(1f))
            InfoBox("Remaining", "${habit.remainingDays} Days", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBox("Started", habit.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), Modifier.weight(1f))
            InfoBox("Ends", habit.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), Modifier.weight(1f))
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConsistencyMap(
    habit: Habit,
    onToggleDate: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val startDate = habit.startDate
    val firstDayOfWeek = startDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val paddingDays = (firstDayOfWeek - 1).coerceAtLeast(0)
    
    val totalDays = habit.durationDays
    val daysList = (0 until totalDays).map { startDate.plusDays(it.toLong()) }
    
    val paddedDays = List(paddingDays) { null } + daysList
    val weeks = paddedDays.chunked(7)

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Consistency Map")
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(modifier = Modifier.height(220.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(weeks, key = { index, _ -> index }) { _, week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                week.forEach { date ->
                                    if (date == null) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    } else {
                                        val isCompleted = habit.completedDates.contains(date)
                                        val isToday = date == today
                                        val isFuture = date.isAfter(today)
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(3.dp)
                                                .background(
                                                    color = when {
                                                        isCompleted -> HabitColors.GRID_HIGH.toCompose()
                                                        isToday -> HabitColors.GRID_LOW.toCompose().copy(alpha = 0.6f)
                                                        isFuture -> Color(0xFFF8F8F8)
                                                        else -> HabitColors.GRID_EMPTY.toCompose()
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .let { 
                                                    if (!isFuture) {
                                                        it.clickable { onToggleDate(date) }
                                                    } else it
                                                }
                                                .let { 
                                                    if (isToday) it.border(1.5.dp, HabitColors.FOREST_DEEP.toCompose(), RoundedCornerShape(6.dp))
                                                    else it
                                                }
                                        )
                                    }
                                }
                                if (week.size < 7) {
                                    repeat(7 - week.size) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap on past days to toggle completion.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun HabitStatistics(habit: Habit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Performance Insights")
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem("Success Rate", "${(habit.completionRate * 100).toInt()}%", Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
                    StatItem("Longest Streak", "${habit.longestStreak}d", Icons.Default.EmojiEvents, Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItem("Total Done", "${habit.totalCompleted}", Icons.Default.CheckCircle, Modifier.weight(1f))
                    StatItem("Missed Days", "${habit.missedDays}", Icons.Default.Cancel, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8F8F8),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(icon, contentDescription = null, tint = HabitColors.EMERALD_SOFT.toCompose(), modifier = Modifier.padding(8.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentLoggedValues(habit: Habit) {
    val recentCompletions = habit.completions.sortedByDescending { it.date }.take(7)
    
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionHeader("Recent Activity")
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (recentCompletions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No activity logged yet", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyRow(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recentCompletions, key = { it.date.toEpochDay() }) { completion ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                completion.date.format(DateTimeFormatter.ofPattern("EEE")),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = HabitColors.EMERALD_SOFT.toCompose().copy(alpha = 0.1f),
                                border = if (completion.date == LocalDate.now()) BorderStroke(1.dp, HabitColors.EMERALD_SOFT.toCompose()) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = completion.value.toString().removeSuffix(".0"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HabitColors.FOREST_DEEP.toCompose()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(HabitColors.ON_SURFACE_TEXT),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
