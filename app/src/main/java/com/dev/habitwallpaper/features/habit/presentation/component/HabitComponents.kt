package com.dev.habitwallpaper.features.habit.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.core.wallpaper.WallpaperConfig
import com.dev.habitwallpaper.domain.model.*
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import java.time.LocalDate
import kotlin.math.ceil

@Composable
fun WallpaperPreview(
    state: WallpaperState,
    customization: WallpaperCustomization,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = when (customization.backgroundType) {
        BackgroundType.SOLID -> Modifier.background(color = Color(customization.backgroundColorStart))
        BackgroundType.GRADIENT -> Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(customization.backgroundColorStart), Color(customization.backgroundColorEnd))
            )
        )
        BackgroundType.PASTEL_GRADIENT -> Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(Color(customization.backgroundColorStart), Color(customization.backgroundColorEnd), Color(0xFFF0F4FF))
            )
        )
        else -> Modifier.background(color = Color(customization.backgroundColorStart))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .then(backgroundModifier)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val verticalBias = when (customization.gridPosition) {
                GridPosition.TOP -> 0.1f
                GridPosition.CENTER -> 0.4f
                GridPosition.BOTTOM -> 0.7f
            }
            
            Spacer(modifier = Modifier.fillMaxHeight(0.08f))
            Text(
                text = "WALLHABIT",
                fontSize = 10.sp,
                color = Color(WallpaperConfig.APP_LABEL_COLOR).copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.habitName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(WallpaperConfig.HABIT_NAME_COLOR)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${state.streakCount} DAY STREAK".uppercase(),
                fontSize = 11.sp,
                color = Color(WallpaperConfig.STREAK_TEXT_COLOR).copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp)) {
                    val grid = state.completionGrid.filter { !it.isPadding }
                    if (grid.isEmpty()) return@Canvas

                    val availableWidth = size.width
                    val availableHeight = size.height
                    
                    val n = grid.size
                    
                    // Base columns on layout type
                    val columns = when (customization.gridLayoutType) {
                        GridLayoutType.COMPACT -> 10
                        GridLayoutType.STANDARD -> 7
                        GridLayoutType.SPACED -> 5
                        GridLayoutType.LARGE_CELLS -> 4
                    }
                    
                    val rows = ceil(n.toFloat() / columns).toInt()
                    
                    val spacingFactor = customization.gridSpacing / 100f
                    
                    val cw = availableWidth / (columns + (columns - 1) * spacingFactor)
                    val ch = availableHeight / (rows + (rows - 1) * spacingFactor)
                    val cellSize = minOf(cw, ch)
                    val spacing = cellSize * spacingFactor
                    
                    val totalGridWidth = (columns * cellSize) + ((columns - 1) * spacing)
                    val totalGridHeight = (rows * cellSize) + ((rows - 1) * spacing)
                    
                    val startX = (availableWidth - totalGridWidth) / 2
                    val startY = (availableHeight - totalGridHeight) * verticalBias

                    for (i in grid.indices) {
                        val row = i / columns
                        val col = i % columns

                        val left = startX + (col * (cellSize + spacing))
                        val top = startY + (row * (cellSize + spacing))
                        val rect = Rect(left, top, left + cellSize, top + cellSize)

                        val isFilled = i < state.totalCompleted
                        val isToday = i == (state.totalCompleted - 1) && customization.highlightToday

                        val color = if (isFilled) {
                            Color(customization.completedCellColor)
                        } else {
                            Color(customization.emptyCellColor).copy(alpha = 0.12f)
                        }

                        drawStudioShape(
                            rect = rect,
                            shape = customization.gridShape,
                            color = color,
                            isFilled = isFilled,
                            emptyColor = Color(customization.emptyCellColor).copy(alpha = 0.3f),
                            showShadow = customization.showShadow,
                            showGlow = customization.showGlow && isFilled,
                            isToday = isToday
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStudioShape(
    rect: Rect,
    shape: GridShape,
    color: Color,
    isFilled: Boolean,
    emptyColor: Color,
    showShadow: Boolean,
    showGlow: Boolean,
    isToday: Boolean
) {
    val style = if (isFilled) Fill else Stroke(width = 1.dp.toPx())
    val finalColor = if (isFilled) color else emptyColor

    when (shape) {
        GridShape.SQUARE -> {
            drawRect(color = finalColor, topLeft = rect.topLeft, size = rect.size, style = style)
        }
        GridShape.ROUNDED_SQUARE -> {
            val cornerRadius = CornerRadius(rect.width * 0.25f)
            drawRoundRect(color = finalColor, topLeft = rect.topLeft, size = rect.size, cornerRadius = cornerRadius, style = style)
        }
        GridShape.CIRCLE -> {
            drawOval(color = finalColor, topLeft = rect.topLeft, size = rect.size, style = style)
        }
        GridShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(rect.center.x, rect.top)
                lineTo(rect.right, rect.center.y)
                lineTo(rect.center.x, rect.bottom)
                lineTo(rect.left, rect.center.y)
                close()
            }
            drawPath(path = path, color = finalColor, style = style)
        }
        GridShape.PIXEL -> {
            drawRect(color = finalColor, topLeft = rect.topLeft, size = rect.size.times(0.9f), style = style)
        }
        GridShape.BUBBLE -> {
            val cornerRadius = CornerRadius(rect.width * 0.5f)
            drawRoundRect(color = finalColor, topLeft = rect.topLeft, size = rect.size, cornerRadius = cornerRadius, style = style)
        }
        GridShape.DOT -> {
            drawCircle(color = finalColor, center = rect.center, radius = rect.width * 0.3f, style = style)
        }
    }
}

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
            val isToday = date == today

            val targetColor = when {
                isBeforeStart -> contentColor.copy(alpha = 0.05f)
                isCompleted -> completedColor ?: habit.color?.let { Color(it) } ?: HabitColors.GRID_HIGH.toCompose()
                else -> missedColor ?: HabitColors.GRID_EMPTY.toCompose()
            }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = if (isToday) 300 else 150),
                label = "CellColor_${habit.id}_${date}"
            )

            val cellScale = remember(habit.id, date) { Animatable(1f) }
            LaunchedEffect(isCompleted, isToday) {
                if (isToday && isCompleted) {
                    cellScale.animateTo(
                        targetValue = 1.25f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                    cellScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .scale(cellScale.value)
                    .background(
                        color = animatedColor,
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}
