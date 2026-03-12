package com.dev.habitwallpaper.core.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.dev.habitwallpaper.domain.model.*
import com.dev.habitwallpaper.domain.usecase.GenerateWallpaperStateUseCase
import com.dev.habitwallpaper.domain.usecase.GetWallpaperCustomizationUseCase
import com.dev.habitwallpaper.domain.usecase.ObserveWallpaperHabitUseCase
import com.dev.habitwallpaper.domain.usecase.WallpaperState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.ceil
import kotlin.math.min
import javax.inject.Inject

@AndroidEntryPoint
class LiveWallpaperService : WallpaperService() {

    @Inject lateinit var observeWallpaperHabitUseCase: ObserveWallpaperHabitUseCase
    @Inject lateinit var generateWallpaperStateUseCase: GenerateWallpaperStateUseCase
    @Inject lateinit var getWallpaperCustomizationUseCase: GetWallpaperCustomizationUseCase

    override fun onCreateEngine(): Engine = HabitWallpaperEngine()

    inner class HabitWallpaperEngine : Engine() {
        private var scope: CoroutineScope? = null
        private var wallpaperState: WallpaperState? = null
        private var customization: WallpaperCustomization = WallpaperCustomization.Default

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
            strokeWidth = dpToPx(1f)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            scope = CoroutineScope(Dispatchers.Main + Job())
            observeData()
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

        private fun observeData() {
            combine(
                observeWallpaperHabitUseCase(),
                getWallpaperCustomizationUseCase()
            ) { habit, customSettings ->
                wallpaperState = generateWallpaperStateUseCase(habit)
                customization = customSettings
            }
            .onEach { draw() }
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
            when (customization.backgroundType) {
                BackgroundType.SOLID -> {
                    canvas.drawColor(customization.backgroundColorStart)
                }
                BackgroundType.GRADIENT -> {
                    backgroundPaint.shader = LinearGradient(
                        0f, 0f, 0f, height,
                        customization.backgroundColorStart,
                        customization.backgroundColorEnd,
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, width, height, backgroundPaint)
                }
                BackgroundType.PASTEL_GRADIENT -> {
                    backgroundPaint.shader = LinearGradient(
                        0f, 0f, width, height,
                        intArrayOf(customization.backgroundColorStart, customization.backgroundColorEnd, 0xFFF0F4FF.toInt()),
                        null,
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, width, height, backgroundPaint)
                }
                else -> {
                    canvas.drawColor(customization.backgroundColorStart)
                }
            }

            val state = wallpaperState ?: return

            val centerX = width / 2

            // 2. Draw App Label
            val appLabelY = height * 0.08f
            canvas.drawText("WALLHABIT", centerX, appLabelY, appLabelPaint)

            // 3. Draw Habit Name
            val habitNameY = appLabelY + spToPx(56f)
            canvas.drawText(state.habitName, centerX, habitNameY, habitNamePaint)

            // 4. Draw Streak Info
            val streakY = habitNameY + spToPx(24f)
            val streakText = "${state.streakCount} DAY STREAK"
            canvas.drawText(streakText.uppercase(), centerX, streakY, streakPaint)

            // 5. Draw Dynamic Progress Grid
            renderDynamicGrid(canvas, state, streakY + dpToPx(48f), height * 0.12f)
        }

        private fun renderDynamicGrid(canvas: Canvas, state: WallpaperState, topLimit: Float, bottomLimit: Float) {
            val grid = state.completionGrid.filter { !it.isPadding }
            if (grid.isEmpty()) return

            val screenWidth = canvas.width.toFloat()
            val screenHeight = canvas.height.toFloat()
            
            val horizontalPadding = screenWidth * 0.12f
            val availableWidth = screenWidth - (horizontalPadding * 2)
            val availableHeight = screenHeight - topLimit - bottomLimit
            
            if (availableWidth <= 0 || availableHeight <= 0) return

            val n = grid.size
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
            val cellSize = min(cw, ch)
            val spacing = cellSize * spacingFactor
            
            val totalGridWidth = (columns * cellSize) + ((columns - 1) * spacing)
            val totalGridHeight = (rows * cellSize) + ((rows - 1) * spacing)
            
            val startX = (screenWidth - totalGridWidth) / 2
            
            val verticalBias = when (customization.gridPosition) {
                GridPosition.TOP -> 0.1f
                GridPosition.CENTER -> 0.4f
                GridPosition.BOTTOM -> 0.7f
            }
            val startY = topLimit + (availableHeight - totalGridHeight) * verticalBias

            for (i in grid.indices) {
                val row = i / columns
                val col = i % columns

                val left = startX + (col * (cellSize + spacing))
                val top = startY + (row * (cellSize + spacing))
                val rect = RectF(left, top, left + cellSize, top + cellSize)

                val isFilled = i < state.totalCompleted
                val isToday = i == (state.totalCompleted - 1) && customization.highlightToday

                if (customization.showShadow) {
                    gridFillPaint.setShadowLayer(dpToPx(2f), 0f, dpToPx(1f), 0x40000000)
                } else {
                    gridFillPaint.clearShadowLayer()
                }

                if (isFilled) {
                    gridFillPaint.color = customization.completedCellColor
                    if (customization.showGlow) {
                         gridFillPaint.setShadowLayer(dpToPx(6f), 0f, 0f, customization.completedCellColor)
                    }
                    if (isToday) {
                        // Highlight today with a stroke or slightly different color
                        gridStrokePaint.color = Color.WHITE
                        gridStrokePaint.alpha = 200
                        drawShape(canvas, rect, customization.gridShape, gridFillPaint)
                        drawShape(canvas, rect, customization.gridShape, gridStrokePaint)
                    } else {
                        drawShape(canvas, rect, customization.gridShape, gridFillPaint)
                    }
                } else {
                    gridFillPaint.color = customization.emptyCellColor
                    gridFillPaint.alpha = 40
                    drawShape(canvas, rect, customization.gridShape, gridFillPaint)
                    
                    gridFillPaint.alpha = 255
                    gridStrokePaint.color = customization.emptyCellColor
                    gridStrokePaint.alpha = 100
                    drawShape(canvas, rect, customization.gridShape, gridStrokePaint)
                }
            }
        }

        private fun drawShape(canvas: Canvas, rect: RectF, shape: GridShape, paint: Paint) {
            when (shape) {
                GridShape.SQUARE -> canvas.drawRect(rect, paint)
                GridShape.ROUNDED_SQUARE -> {
                    val cornerRadius = rect.width() * 0.25f
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                }
                GridShape.CIRCLE -> canvas.drawOval(rect, paint)
                GridShape.DIAMOND -> {
                    val path = Path()
                    path.moveTo(rect.centerX(), rect.top)
                    path.lineTo(rect.right, rect.centerY())
                    path.lineTo(rect.centerX(), rect.bottom)
                    path.lineTo(rect.left, rect.centerY())
                    path.close()
                    canvas.drawPath(path, paint)
                }
                GridShape.PIXEL -> {
                    val pixelRect = RectF(rect.left, rect.top, rect.left + rect.width() * 0.9f, rect.top + rect.height() * 0.9f)
                    canvas.drawRect(pixelRect, paint)
                }
                GridShape.BUBBLE -> {
                    val cornerRadius = rect.width() * 0.5f
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                }
                GridShape.DOT -> {
                    canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() * 0.3f, paint)
                }
            }
        }

        private fun spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity
        private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
    }
}
