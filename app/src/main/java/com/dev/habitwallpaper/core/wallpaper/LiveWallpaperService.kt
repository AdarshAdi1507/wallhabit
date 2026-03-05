package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.dev.habitwallpaper.HabitApplication
import com.dev.habitwallpaper.domain.usecase.GenerateWallpaperStateUseCase
import com.dev.habitwallpaper.domain.usecase.GridCellState
import com.dev.habitwallpaper.domain.usecase.ObserveWallpaperHabitUseCase
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.min

class LiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = HabitWallpaperEngine()

    inner class HabitWallpaperEngine : Engine() {
        private var scope: CoroutineScope? = null
        private var wallpaperState: WallpaperState? = null

        private val backgroundPaint = Paint()
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.TEXT_PRIMARY
            isFakeBoldText = true
        }
        private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.TEXT_SECONDARY
        }
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            scope = CoroutineScope(Dispatchers.Main + Job())
            observeWallpaperHabit()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            scope?.cancel()
            scope = null
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                draw()
            }
        }

        private fun observeWallpaperHabit() {
            val app = application as HabitApplication
            val repository = app.repository
            val observeUseCase = ObserveWallpaperHabitUseCase(repository)
            val generateUseCase = GenerateWallpaperStateUseCase()

            observeUseCase()
                .onEach { habit ->
                    wallpaperState = generateUseCase(habit)
                    draw()
                }
                .launchIn(scope!!)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    render(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun render(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()

            // 1. Draw Background
            backgroundPaint.shader = LinearGradient(
                0f, 0f, 0f, height,
                WallpaperConfig.BACKGROUND_GRADIENT_START,
                WallpaperConfig.BACKGROUND_GRADIENT_END,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width, height, backgroundPaint)

            val state = wallpaperState ?: return

            // 2. Draw Header Text (Positioned at Top)
            val centerX = width / 2
            val titleY = height * 0.15f
            
            titlePaint.textSize = spToPx(WallpaperConfig.TITLE_TEXT_SIZE_SP)
            canvas.drawText(state.habitName.uppercase(), centerX, titleY, titlePaint)

            subtitlePaint.textSize = spToPx(WallpaperConfig.SUBTITLE_TEXT_SIZE_SP)
            canvas.drawText("${state.streakCount} DAY STREAK", centerX, titleY + 60f, subtitlePaint)

            // 3. Draw Artwork Grid
            renderArtworkGrid(canvas, state.completionGrid)
        }

        private fun renderArtworkGrid(canvas: Canvas, grid: List<GridCellState>) {
            if (grid.isEmpty()) return

            val screenWidth = canvas.width.toFloat()
            val screenHeight = canvas.height.toFloat()
            
            // Define drawing area margins
            val leftMargin = screenWidth * WallpaperConfig.GRID_SIDE_MARGIN_PERCENT
            val rightMargin = screenWidth * WallpaperConfig.GRID_SIDE_MARGIN_PERCENT
            val topMargin = screenHeight * WallpaperConfig.GRID_TOP_MARGIN_PERCENT
            val bottomMargin = screenHeight * WallpaperConfig.GRID_BOTTOM_MARGIN_PERCENT
            
            val availableWidth = screenWidth - leftMargin - rightMargin
            val availableHeight = screenHeight - topMargin - bottomMargin
            
            val columns = WallpaperConfig.GRID_COLUMNS
            val numRows = (grid.size + columns - 1) / columns
            
            val spacing = dpToPx(WallpaperConfig.GRID_SPACING_DP)
            
            // Calculate largest possible square size
            val cellWidth = (availableWidth - (columns - 1) * spacing) / columns
            val cellHeight = (availableHeight - (numRows - 1) * spacing) / numRows
            
            val cellSize = min(cellWidth, cellHeight)
            val cornerRadius = cellSize * 0.15f
            
            // Center the grid within the available area
            val totalGridWidth = (columns * cellSize) + ((columns - 1) * spacing)
            val totalGridHeight = (numRows * cellSize) + ((numRows - 1) * spacing)
            
            val startX = leftMargin + (availableWidth - totalGridWidth) / 2
            val startY = topMargin + (availableHeight - totalGridHeight) / 2

            for (i in grid.indices) {
                val cell = grid[i]
                if (cell.isPadding) continue

                val row = i / columns
                val col = i % columns

                val left = startX + (col * (cellSize + spacing))
                val top = startY + (row * (cellSize + spacing))
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                // If completed -> Theme Color, else -> Neutral Background
                gridPaint.color = if (cell.isCompleted) {
                    WallpaperConfig.getThemeColor(i, columns)
                } else {
                    WallpaperConfig.GRID_NEUTRAL
                }
                
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gridPaint)
                
                // Highlight "Today"
                if (cell.isToday) {
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = dpToPx(2f)
                        color = WallpaperConfig.TEXT_PRIMARY
                    }
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
                }
            }
        }

        private fun spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity
        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
}
