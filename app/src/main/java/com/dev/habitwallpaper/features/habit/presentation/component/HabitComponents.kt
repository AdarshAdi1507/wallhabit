package com.dev.habitwallpaper.features.habit.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.core.wallpaper.WallpaperConfig
import com.dev.habitwallpaper.domain.model.Habit
import java.time.LocalDate
import kotlin.math.ceil

@Composable
fun MiniWallpaperPreview(habit: Habit, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(8.dp)) {
        val n = habit.durationDays
        if (n <= 0) return@Canvas

        val availableWidth = size.width
        val availableHeight = size.height
        
        var bestCols = 1
        var bestRows = n
        var maxCellSize = 0f
        val spacingFactor = 0.15f

        for (cols in 1..n) {
            val rows = ceil(n.toFloat() / cols).toInt()
            val cw = availableWidth / (cols + (cols - 1) * spacingFactor)
            val ch = availableHeight / (rows + (rows - 1) * spacingFactor)
            val currentCellSize = minOf(cw, ch)
            
            if (currentCellSize > maxCellSize) {
                maxCellSize = currentCellSize
                bestCols = cols
                bestRows = rows
            }
        }

        val cellSize = maxCellSize
        val spacing = cellSize * spacingFactor
        val cornerRadius = cellSize * 0.25f
        
        val totalGridWidth = (bestCols * cellSize) + ((bestCols - 1) * spacing)
        val totalGridHeight = (bestRows * cellSize) + ((bestRows - 1) * spacing)
        
        val startX = (availableWidth - totalGridWidth) / 2
        val startY = (availableHeight - totalGridHeight) / 2

        for (i in 0 until n) {
            val row = i / bestCols
            val col = i % bestCols
            
            val isCompleted = i < habit.totalCompleted
            
            val color = if (isCompleted) {
                Color(WallpaperConfig.GRID_COMPLETED)
            } else {
                Color(0xFFE0E0E0)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    startX + col * (cellSize + spacing),
                    startY + row * (cellSize + spacing)
                ),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(cornerRadius),
                style = Fill
            )
        }
    }
}

@Composable
fun ConsistencyIndicatorRow(
    habit: Habit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    completedColor: Color? = null,
    missedColor: Color? = null
) {
    val today = LocalDate.now()
    val last7Days = (0..6).reversed().map { today.minusDays(it.toLong()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        last7Days.forEach { date ->
            val isBeforeStart = date.isBefore(habit.startDate)
            val isCompleted = habit.completedDates.contains(date)
            
            val boxColor = when {
                isBeforeStart -> contentColor.copy(alpha = 0.05f)
                isCompleted -> completedColor ?: habit.color?.let { Color(it) } ?: HabitColors.GRID_HIGH.toCompose()
                else -> missedColor ?: HabitColors.GRID_EMPTY.toCompose()
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
