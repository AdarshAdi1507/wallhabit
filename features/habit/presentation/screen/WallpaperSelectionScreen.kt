package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.wallpaper.WallpaperConfig
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WallpaperSelectionViewModel
import java.time.LocalDate
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperSelectionScreen(
    viewModel: WallpaperSelectionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Wallpaper Habit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.habits) { habit ->
                    WallpaperHabitItem(
                        habit = habit,
                        isSelected = habit.isWallpaperSelected,
                        onSelect = { viewModel.selectWallpaperHabit(habit.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperHabitItem(
    habit: Habit,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature Wallpaper Preview - Fixed Container
            Box(
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                MiniWallpaperPreview(habit, Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${habit.currentStreak} day streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "PRIORITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MiniWallpaperPreview(habit: Habit, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(8.dp)) {
        val totalDays = habit.durationDays
        if (totalDays <= 0) return@Canvas

        val columns = 7
        val rows = ceil(totalDays.toFloat() / columns).toInt()
        
        val spacing = 2.dp.toPx()
        val cellSize = (size.width - (columns - 1) * spacing) / columns
        val cornerRadius = cellSize * 0.25f
        
        val totalGridHeight = (rows * cellSize) + ((rows - 1) * spacing)
        val startY = (size.height - totalGridHeight) / 2

        for (i in 0 until totalDays) {
            val row = i / columns
            val col = i % columns
            
            val isCompleted = i < habit.totalCompleted
            
            val color = if (isCompleted) {
                Color(WallpaperConfig.GRID_COMPLETED)
            } else {
                Color(0xFFE0E0E0)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    col * (cellSize + spacing),
                    startY + row * (cellSize + spacing)
                ),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = Fill
            )
        }
    }
}
