package com.dev.habitwallpaper.features.habit.presentation.detail

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.min
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.core.wallpaper.LiveWallpaperService
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.habit?.name ?: "Habit Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.habit?.let { habit ->
                        IconButton(onClick = { viewModel.setAsWallpaper() }) {
                            Icon(
                                imageVector = if (habit.isWallpaperSelected) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Select for Wallpaper",
                                tint = if (habit.isWallpaperSelected) Color(0xFFFFD700) else Color.Gray
                            )
                        }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Streak Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = HabitColors.STREAK_ORANGE.toCompose(),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "${habit.currentStreak}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HabitColors.STREAK_ORANGE.toCompose()
                                )
                                Text(
                                    text = "Current Day Streak",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Progress Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoCard(
                                label = "Goal",
                                value = "${habit.durationDays} Days",
                                modifier = Modifier.weight(1.0f)
                            )
                            InfoCard(
                                label = "Started",
                                value = habit.startDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                                modifier = Modifier.weight(1.0f)
                            )
                        }

                        // Heatmap Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Consistency Map",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            DetailedHeatMap(
                                startDate = habit.startDate,
                                durationDays = habit.durationDays,
                                completedDates = habit.completedDates
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons Column
                    Column(
                        modifier = Modifier.padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Wallpaper Action Button
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.Wallpaper, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview & Set Live Wallpaper", fontWeight = FontWeight.Bold)
                        }

                        // Toggle Button
                        Button(
                            onClick = { viewModel.toggleCompletion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            val isDone = habit.isCompletedToday
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (isDone) {
                                            Brush.horizontalGradient(listOf(Color(0xFFBDBDBD), Color(0xFFE0E0E0)))
                                        } else {
                                            Brush.horizontalGradient(listOf(HabitColors.FOREST_DEEP.toCompose(), HabitColors.EMERALD_SOFT.toCompose()))
                                        },
                                        shape = RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isDone) "Completed for Today" else "Mark as Done",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
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
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailedHeatMap(
    startDate: LocalDate,
    durationDays: Int,
    completedDates: List<LocalDate>
) {
    val today = LocalDate.now()
    val firstDayOfWeek = startDate.dayOfWeek.value
    val paddingDays = firstDayOfWeek - 1
    val totalItems = (paddingDays + durationDays).coerceAtLeast(1)
    val rows = (0 until totalItems).chunked(7)
    val numRows = rows.size.coerceAtLeast(1)

    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (numRows > 15) 320.dp else 240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight.coerceAtLeast(200.dp) - 32.dp
            
            val spacing = when {
                numRows > 30 -> 1.dp
                numRows > 15 -> 2.dp
                numRows > 8 -> 3.dp
                else -> 4.dp
            }

            val cellWidth = (availableWidth - (spacing * 6)) / 7
            val cellHeight = (availableHeight - (spacing * (numRows - 1))) / numRows
            
            val cellSize = min(cellWidth, cellHeight).coerceIn(2.dp, 40.dp)
            val cornerRadius = (cellSize / 4).coerceAtMost(4.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically)
            ) {
                // Header row
                Row(
                    modifier = Modifier.width(cellSize * 7 + spacing * 6),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.width(cellSize),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (cellSize.value * 0.45f).coerceIn(7f, 11f).sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }

                // Grid rows
                rows.forEach { weekIndices ->
                    Row(
                        modifier = Modifier.width(cellSize * 7 + spacing * 6),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        weekIndices.forEach { index ->
                            if (index < paddingDays) {
                                Spacer(modifier = Modifier.size(cellSize))
                            } else {
                                val date = startDate.plusDays((index - paddingDays).toLong())
                                val isCompleted = completedDates.contains(date)
                                val isToday = date == today
                                val isFuture = date.isAfter(today)

                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .background(
                                            color = when {
                                                isCompleted -> HabitColors.GRID_HIGH.toCompose()
                                                isToday -> HabitColors.GRID_LOW.toCompose().copy(alpha = 0.6f)
                                                isFuture -> Color(0xFFF8F8F8)
                                                else -> HabitColors.GRID_EMPTY.toCompose()
                                            },
                                            shape = RoundedCornerShape(cornerRadius)
                                        )
                                )
                            }
                        }
                        if (weekIndices.size < 7) {
                            repeat(7 - weekIndices.size) {
                                Spacer(modifier = Modifier.size(cellSize))
                            }
                        }
                    }
                }
            }
        }
    }
}
