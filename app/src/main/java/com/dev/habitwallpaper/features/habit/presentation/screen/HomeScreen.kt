package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.domain.model.TrackingType
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeViewModel
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HomeUiState
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.QuoteUiState
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.QuoteViewModel
import com.dev.habitwallpaper.features.habit.presentation.component.MiniWallpaperPreview
import com.dev.habitwallpaper.features.habit.presentation.component.ConsistencyIndicatorRow
import com.dev.habitwallpaper.features.habit.presentation.util.icon
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    quoteViewModel: QuoteViewModel,
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onWallpaperClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quoteUiState by quoteViewModel.uiState.collectAsState()
    var habitForNumericInput by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "HabitFlow",
                        style = MaterialTheme.typography.titleLarge,
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.habits.isEmpty()) {
            EmptyState(onAddHabit, innerPadding)
        } else {
            HomeContent(
                uiState = uiState,
                quoteUiState = quoteUiState,
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
fun UserGreeting(name: String) {
    Text(
        text = "Hi, $name",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    quoteUiState: QuoteUiState,
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
        // 0. User greeting
        uiState.userName?.takeIf { it.isNotBlank() }?.let { name ->
            item(key = "greeting") {
                UserGreeting(name = name)
            }
        }

        // 0b. Motivational quote card
        item(key = "quote") {
            MotivationCard(quoteUiState = quoteUiState)
        }

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

        items(uiState.habits, key = { it.id }) { habit ->
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

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Extra space for bottom nav
        }
    }
}

@Composable
fun MotivationCard(quoteUiState: QuoteUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        when (quoteUiState) {
            is QuoteUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            is QuoteUiState.Success -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = quoteUiState.quote.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "— ${quoteUiState.quote.author}",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            is QuoteUiState.Error -> {
                // Silent — the card simply stays empty on a persistent error
            }
        }
    }
}

@Composable
fun FocusHabitCard(habit: Habit, onToggle: () -> Unit) {
    val isCompleted = habit.isCompletedToday
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val cardScale = remember { Animatable(1f) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "FocusCardBackground"
    )
    val contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale.value),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // 1. Section Label
            Text(
                "TODAY'S FOCUS",
                style = MaterialTheme.typography.labelLarge,
                color = if (isCompleted) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Habit Identity Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isCompleted) contentColor.copy(alpha = 0.2f) else habit.color?.let { Color(it).copy(alpha = 0.2f) } ?: MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habit.category.icon,
                        contentDescription = null,
                        tint = if (isCompleted) contentColor else habit.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    // 3. Streak and Progress Information
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = if (isCompleted) contentColor.copy(alpha = 0.8f) else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habit.currentStreak} day streak • Day ${habit.currentDay} of ${habit.durationDays}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCompleted) contentColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Recent Activity Preview (Last 7 Days)
            ConsistencyIndicatorRow(
                habit = habit,
                contentColor = contentColor,
                completedColor = if (isCompleted) contentColor else null,
                missedColor = if (isCompleted) contentColor.copy(alpha = 0.2f) else null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Completion Status
            if (isCompleted) {
                Surface(
                    color = contentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Completed today",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            cardScale.animateTo(
                                targetValue = 1.04f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessHigh
                                )
                            )
                            cardScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        onToggle()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) contentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                        contentColor = if (isCompleted) contentColor else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Today", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    color = Color(0xFF56AB2F),
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
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Card scale animation — bounces gently on completion tap
    val cardScale = remember { Animatable(1f) }

    // Show a brief "✓ Completed today" toast-like banner
    var showSuccessBanner by remember(habit.id) { mutableStateOf(false) }

    // Animated completion button background colour
    val buttonBg by animateColorAsState(
        targetValue = if (habit.isCompletedToday)
            MaterialTheme.colorScheme.primary
        else
            Color(0xFFF0F0F0),
        animationSpec = tween(durationMillis = 300),
        label = "CompletionButtonBg_${habit.id}"
    )
    val buttonIcon = if (habit.isCompletedToday) Icons.Default.Check else Icons.Default.Add
    val buttonIconTint = if (habit.isCompletedToday)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale.value)
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
                                color = habit.color?.let { Color(it).copy(alpha = 0.2f) }
                                    ?: MaterialTheme.colorScheme.primaryContainer,
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
                    onClick = {
                        if (!habit.isCompletedToday) {
                            // Trigger haptic confirmation
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Bounce the card scale: 1.0 → 1.05 → 1.0
                            coroutineScope.launch {
                                cardScale.animateTo(
                                    targetValue = 1.05f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    )
                                )
                                cardScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                            // Show success banner and auto-dismiss after 2 seconds
                            coroutineScope.launch {
                                showSuccessBanner = true
                                delay(2000)
                                showSuccessBanner = false
                            }
                        }
                        onToggle()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = buttonBg, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = "Complete",
                        tint = buttonIconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Success banner: fades in + scales in, auto-dismissed
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit = fadeOut(tween(250)) + scaleOut(tween(250))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "✓ Completed today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "🔥 ${habit.currentStreak + 1} Day Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = HabitColors.STREAK_ORANGE.toCompose(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Already-completed persistent badge (shown after banner auto-dismissed)
            if (habit.isCompletedToday && !showSuccessBanner) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300))
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Completed today  •  🔥 ${habit.currentStreak} day streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HabitConsistencyPreview(habit)
        }
    }
}

@Composable
fun HabitConsistencyPreview(habit: Habit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Last 7 days",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ConsistencyIndicatorRow(habit)
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
fun EmptyState(onAddHabit: () -> Unit, innerPadding: PaddingValues = PaddingValues()) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
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
                textAlign = TextAlign.Center,
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
