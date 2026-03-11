package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.dev.habitwallpaper.domain.usecase.GenerateWallpaperStateUseCase
import com.dev.habitwallpaper.domain.usecase.GridCellState
import com.dev.habitwallpaper.domain.usecase.ObserveWallpaperHabitUseCase
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.ceil
import kotlin.math.min
import javax.inject.Inject

@AndroidEntryPoint
class LiveWallpaperService : WallpaperService() {

    @Inject lateinit var observeWallpaperHabitUseCase: ObserveWallpaperHabitUseCase
    @Inject lateinit var generateWallpaperStateUseCase: GenerateWallpaperStateUseCase

    override fun onCreateEngine(): Engine = HabitWallpaperEngine()

    inner class HabitWallpaperEngine : Engine() {
        private var scope: CoroutineScope? = null
        private var wallpaperState: WallpaperState? = null

        private val backgroundPaint = Paint()
        
        private val appLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.APP_LABEL_COLOR
            textSize = spToPx(WallpaperConfig.APP_LABEL_SIZE_SP)
            letterSpacing = WallpaperConfig.APP_LABEL_LETTER_SPACING
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        private val habitNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.HABIT_NAME_COLOR
            textSize = spToPx(WallpaperConfig.HABIT_NAME_SIZE_SP)
            letterSpacing = WallpaperConfig.HABIT_NAME_LETTER_SPACING
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.STREAK_TEXT_COLOR
            textSize = spToPx(WallpaperConfig.STREAK_TEXT_SIZE_SP)
            letterSpacing = WallpaperConfig.STREAK_TEXT_LETTER_SPACING
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        private val gridFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        private val gridStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = WallpaperConfig.GRID_INCOMPLETE_BORDER
            strokeWidth = dpToPx(1f)
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
            observeWallpaperHabitUseCase()
                .onEach { habit ->
                    wallpaperState = generateWallpaperStateUseCase(habit)
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

            // 1. Draw Background Gradient
            backgroundPaint.shader = LinearGradient(
                0f, 0f, 0f, height,
                WallpaperConfig.BACKGROUND_GRADIENT_START,
                WallpaperConfig.BACKGROUND_GRADIENT_END,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, width, height, backgroundPaint)

            val state = wallpaperState ?: return

            val centerX = width / 2

            // 2. Draw App Label (Top Center)
            val appLabelY = height * 0.08f
            canvas.drawText("WALLHABIT", centerX, appLabelY, appLabelPaint)

            // 3. Draw Habit Name
            val habitNameY = appLabelY + spToPx(56f)
            canvas.drawText(state.habitName, centerX, habitNameY, habitNamePaint)

            // 4. Draw Streak Info (Uppercase)
            val streakY = habitNameY + spToPx(24f)
            val streakText = "${state.streakCount} DAY STREAK"
            canvas.drawText(streakText.uppercase(), centerX, streakY, streakPaint)

            // 5. Draw Dynamic Progress Grid
            val gridTopLimit = streakY + dpToPx(48f)
            val bottomLimit = height * 0.12f // Action button area safety
            renderDynamicGrid(canvas, state, gridTopLimit, bottomLimit)
        }

        private fun renderDynamicGrid(canvas: Canvas, state: WallpaperState, topLimit: Float, bottomLimit: Float) {
            val grid = state.completionGrid.filter { !it.isPadding }
            if (grid.isEmpty()) return

            val screenWidth = canvas.width.toFloat()
            val screenHeight = canvas.height.toFloat()
            
            val horizontalPadding = screenWidth * WallpaperConfig.GRID_SIDE_MARGIN_PERCENT
            val availableWidth = screenWidth - (horizontalPadding * 2)
            val availableHeight = screenHeight - topLimit - bottomLimit
            
            if (availableWidth <= 0 || availableHeight <= 0) return

            val n = grid.size
            var bestCols = 1
            var bestRows = n
            var maxCellSize = 0f
            val spacingFactor = 0.15f // 15% spacing

            // Calculate optimal grid layout (maximize square cell size)
            for (cols in 1..n) {
                val rows = ceil(n.toFloat() / cols).toInt()
                val cw = availableWidth / (cols + (cols - 1) * spacingFactor)
                val ch = availableHeight / (rows + (rows - 1) * spacingFactor)
                val currentCellSize = min(cw, ch)
                
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
            
            val startX = (screenWidth - totalGridWidth) / 2
            val startY = topLimit + (availableHeight - totalGridHeight) / 2

            for (i in grid.indices) {
                val row = i / bestCols
                val col = i % bestCols

                val left = startX + (col * (cellSize + spacing))
                val top = startY + (row * (cellSize + spacing))
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                val isFilled = i < state.totalCompleted

                if (isFilled) {
                    gridFillPaint.color = WallpaperConfig.GRID_COMPLETED
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gridFillPaint)
                } else {
                    gridFillPaint.color = WallpaperConfig.GRID_INCOMPLETE_BG
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gridFillPaint)
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gridStrokePaint)
                }
            }
        }

        private fun spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity
        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
}
