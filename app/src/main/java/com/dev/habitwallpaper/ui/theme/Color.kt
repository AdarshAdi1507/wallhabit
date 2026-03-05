package com.dev.habitwallpaper.ui.theme

import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose

// Brand Colors
val ForestDeep = HabitColors.FOREST_DEEP.toCompose()
val EmeraldSoft = HabitColors.EMERALD_SOFT.toCompose()
val MintSurface = HabitColors.MINT_SURFACE.toCompose()
val DarkGrayText = HabitColors.ON_SURFACE_TEXT.toCompose()
val MutedSageText = HabitColors.SECONDARY_TEXT.toCompose()

// Dark Mode Colors
val DarkGreenBlack = HabitColors.DARK_GREEN_BLACK.toCompose()
val GlowEmerald = HabitColors.GLOW_EMERALD.toCompose()

// Heatmap Colors
val GridEmpty = HabitColors.GRID_EMPTY.toCompose()
val GridLow = HabitColors.GRID_LOW.toCompose()
val GridMedium = HabitColors.GRID_MEDIUM.toCompose()
val GridHigh = HabitColors.GRID_HIGH.toCompose()

// Legacy mappings for Material Theme backward compatibility
val Purple80 = GlowEmerald
val PurpleGrey80 = EmeraldSoft
val Pink80 = GridLow

val Purple40 = ForestDeep
val PurpleGrey40 = EmeraldSoft
val Pink40 = DarkGrayText
