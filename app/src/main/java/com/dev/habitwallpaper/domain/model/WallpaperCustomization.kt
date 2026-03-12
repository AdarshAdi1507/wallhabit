package com.dev.habitwallpaper.domain.model

import com.dev.habitwallpaper.core.wallpaper.WallpaperConfig

enum class GridShape {
    SQUARE, ROUNDED_SQUARE, CIRCLE, DIAMOND, PIXEL, BUBBLE, DOT
}

enum class BackgroundType {
    SOLID, GRADIENT, PASTEL_GRADIENT, NOISE, GLASS_BLUR, PAPER_TEXTURE
}

enum class ProgressStyle {
    SOLID, INTENSITY
}

enum class GridLayoutType {
    COMPACT, STANDARD, SPACED, LARGE_CELLS
}

enum class GridPosition {
    TOP, CENTER, BOTTOM
}

data class WallpaperCustomization(
    val themeType: String = "Default",
    val backgroundType: BackgroundType = BackgroundType.GRADIENT,
    val gridShape: GridShape = GridShape.ROUNDED_SQUARE,
    val completedCellColor: Int = WallpaperConfig.GRID_COMPLETED,
    val emptyCellColor: Int = WallpaperConfig.GRID_INCOMPLETE_BG,
    val paletteName: String = "Matcha Green",
    val progressStyle: ProgressStyle = ProgressStyle.SOLID,
    val gridLayoutType: GridLayoutType = GridLayoutType.STANDARD,
    val gridSpacing: Float = WallpaperConfig.GRID_SPACING_DP,
    val gridPosition: GridPosition = GridPosition.CENTER,
    val backgroundColorStart: Int = WallpaperConfig.BACKGROUND_GRADIENT_START,
    val backgroundColorEnd: Int = WallpaperConfig.BACKGROUND_GRADIENT_END,
    val showGlow: Boolean = false,
    val showShadow: Boolean = false,
    val highlightToday: Boolean = false,
    val pulseEffect: Boolean = false
) {
    companion object {
        val Default = WallpaperCustomization()
        
        val MinimalFocus = WallpaperCustomization(
            themeType = "Minimal Focus",
            backgroundType = BackgroundType.SOLID,
            backgroundColorStart = 0xFFF8F9FA.toInt(),
            completedCellColor = 0xFF212121.toInt(),
            emptyCellColor = 0x1A000000.toInt(),
            gridShape = GridShape.SQUARE,
            gridLayoutType = GridLayoutType.SPACED
        )
        
        val MatchaCalm = WallpaperCustomization(
            themeType = "Matcha Calm",
            backgroundType = BackgroundType.GRADIENT,
            backgroundColorStart = 0xFFE8F5E9.toInt(),
            backgroundColorEnd = 0xFFC8E6C9.toInt(),
            completedCellColor = 0xFF4CAF50.toInt(),
            gridShape = GridShape.BUBBLE
        )

        val DarkDiscipline = WallpaperCustomization(
            themeType = "Dark Discipline",
            backgroundType = BackgroundType.SOLID,
            backgroundColorStart = 0xFF121212.toInt(),
            completedCellColor = 0xFFBB86FC.toInt(),
            emptyCellColor = 0x1AFFFFFF.toInt(),
            showGlow = true
        )

        val EmeraldGlow = WallpaperCustomization(
            themeType = "Emerald Glow",
            backgroundType = BackgroundType.GRADIENT,
            backgroundColorStart = 0xFF004D40.toInt(),
            backgroundColorEnd = 0xFF002420.toInt(),
            completedCellColor = 0xFF00C853.toInt(),
            showGlow = true,
            gridShape = GridShape.CIRCLE
        )

        val PastelGradient = WallpaperCustomization(
            themeType = "Pastel Gradient",
            backgroundType = BackgroundType.PASTEL_GRADIENT,
            backgroundColorStart = 0xFFFFE0E0.toInt(),
            backgroundColorEnd = 0xFFE0E0FF.toInt(),
            completedCellColor = 0xFFFF80AB.toInt(),
            gridShape = GridShape.DOT
        )

        val GlassMorphic = WallpaperCustomization(
            themeType = "Glass Morphic",
            backgroundType = BackgroundType.GLASS_BLUR,
            backgroundColorStart = 0xFF3F51B5.toInt(),
            backgroundColorEnd = 0xFF2196F3.toInt(),
            completedCellColor = 0xFFFFFFFF.toInt(),
            emptyCellColor = 0x33FFFFFF.toInt(),
            showShadow = true
        )
    }
}
