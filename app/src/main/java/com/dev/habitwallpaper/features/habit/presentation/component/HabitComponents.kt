package com.dev.habitwallpaper.features.habit.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.dev.habitwallpaper.core.wallpaper.WallpaperConfig
import com.dev.habitwallpaper.domain.model.Habit
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
