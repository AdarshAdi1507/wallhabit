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
import com.dev.habitwallpaper.domain.usecase.ObserveWallpaperHabitUseCase
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = HabitWallpaperEngine()

    inner class HabitWallpaperEngine : Engine() {
        private var scope: CoroutineScope? = null
        private var wallpaperState: WallpaperState? = null

        private val backgroundPaint = Paint()
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.TEXT_PRIMARY
        }
        private val streakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = WallpaperConfig.STREAK_ORANGE
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

            // 2. Draw Text (Centered with vertical bias)
            val centerX = width / 2
            val centerY = height * WallpaperConfig.VERTICAL_BIAS

            titlePaint.textSize = spToPx(WallpaperConfig.TITLE_TEXT_SIZE_SP)
            canvas.drawText(state.habitName, centerX, centerY, titlePaint)

            streakPaint.textSize = spToPx(WallpaperConfig.SUBTITLE_TEXT_SIZE_SP)
            canvas.drawText("${state.streakCount} Day Streak", centerX, centerY + 80f, streakPaint)

            // 3. Draw Heatmap Grid
            renderGrid(canvas, centerX, centerY + 180f, state.completionGrid)
        }

        private fun renderGrid(canvas: Canvas, centerX: Float, startY: Float, grid: List<Boolean>) {
            val cellSize = dpToPx(WallpaperConfig.GRID_CELL_SIZE_DP)
            val spacing = dpToPx(WallpaperConfig.GRID_SPACING_DP)
            val cornerRadius = dpToPx(4f)
            
            val totalGridWidth = (WallpaperConfig.GRID_COLUMNS * cellSize) + ((WallpaperConfig.GRID_COLUMNS - 1) * spacing)
            val startX = centerX - (totalGridWidth / 2)

            // Reverse the grid so the most recent day (today) is at the bottom right
            val displayGrid = grid.reversed()

            for (i in displayGrid.indices) {
                if (i >= WallpaperConfig.GRID_COLUMNS * WallpaperConfig.GRID_ROWS) break

                val row = i / WallpaperConfig.GRID_COLUMNS
                val col = i % WallpaperConfig.GRID_COLUMNS

                val left = startX + (col * (cellSize + spacing))
                val top = startY + (row * (cellSize + spacing))
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                gridPaint.color = if (displayGrid[i]) WallpaperConfig.GRID_FILLED else WallpaperConfig.GRID_EMPTY
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, gridPaint)
            }
        }

        private fun spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity
        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
}
