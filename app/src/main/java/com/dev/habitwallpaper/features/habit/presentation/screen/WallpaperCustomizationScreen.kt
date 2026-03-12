package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.habitwallpaper.domain.model.*
import com.dev.habitwallpaper.features.habit.presentation.component.WallpaperPreview
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WallpaperCustomizationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCustomizationScreen(
    onBack: () -> Unit,
    viewModel: WallpaperCustomizationViewModel = hiltViewModel()
) {
    val customization by viewModel.customization.collectAsState()
    val previewState by viewModel.previewState.collectAsState()
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val scrollToPreview = {
        scope.launch {
            scrollState.animateScrollTo(
                0,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Design Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.resetToDefault() 
                        scrollToPreview()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            viewModel.resetToDefault()
                            scrollToPreview()
                        },
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { 
                            viewModel.applyWallpaper()
                            onBack()
                        },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Apply Wallpaper")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Live Preview Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(9f / 19f)
                        .shadow(24.dp, RoundedCornerShape(32.dp))
                        .border(6.dp, Color.Black, RoundedCornerShape(32.dp))
                        .padding(4.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Black
                ) {
                    previewState?.let { state ->
                        WallpaperPreview(
                            state = state,
                            customization = customization,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                StudioSection(title = "Themes", icon = Icons.Default.Palette) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val themes = listOf(
                            "Default" to WallpaperCustomization.Default,
                            "Minimal Focus" to WallpaperCustomization.MinimalFocus,
                            "Matcha Calm" to WallpaperCustomization.MatchaCalm,
                            "Dark Discipline" to WallpaperCustomization.DarkDiscipline,
                            "Emerald Glow" to WallpaperCustomization.EmeraldGlow,
                            "Pastel Gradient" to WallpaperCustomization.PastelGradient,
                            "Glass Morphic" to WallpaperCustomization.GlassMorphic
                        )
                        items(themes) { (name, preset) ->
                            StudioOptionCard(
                                title = name,
                                isSelected = customization.themeType == name,
                                onClick = { 
                                    viewModel.updateCustomization(preset)
                                    scrollToPreview()
                                },
                                visual = { ThemeVisualPreview(preset) }
                            )
                        }
                    }
                }

                StudioSection(title = "Grid Shape", icon = Icons.Default.Category) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(GridShape.entries) { shape ->
                            StudioOptionCard(
                                title = shape.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = customization.gridShape == shape,
                                onClick = { 
                                    viewModel.updateCustomization(customization.copy(gridShape = shape))
                                    scrollToPreview()
                                },
                                visual = { ShapeVisualPreview(shape, customization.gridShape == shape) }
                            )
                        }
                    }
                }

                StudioSection(title = "Grid Layout", icon = Icons.Default.GridView) {
                    Column {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(GridLayoutType.entries) { layout ->
                                StudioOptionCard(
                                    title = layout.name.lowercase().replaceFirstChar { it.uppercase() },
                                    isSelected = customization.gridLayoutType == layout,
                                    onClick = { 
                                        viewModel.updateCustomization(customization.copy(gridLayoutType = layout))
                                        scrollToPreview()
                                    },
                                    visual = { LayoutVisualPreview(layout) }
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Spacing", 
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))
                            Slider(
                                value = customization.gridSpacing,
                                onValueChange = { 
                                    viewModel.updateCustomization(customization.copy(gridSpacing = it))
                                },
                                onValueChangeFinished = { scrollToPreview() },
                                valueRange = 2f..30f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                StudioSection(title = "Color Palette", icon = Icons.Default.ColorLens) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val palettes = listOf(
                            "Matcha Green" to 0xFF4CAF50.toInt(),
                            "Mint Fresh" to 0xFF58D68D.toInt(),
                            "Emerald Focus" to 0xFF00C853.toInt(),
                            "Soft Pastel" to 0xFFFF80AB.toInt(),
                            "Forest Calm" to 0xFF2E7D32.toInt(),
                            "Dark Neon" to 0xFFBB86FC.toInt()
                        )
                        items(palettes) { (name, color) ->
                            StudioOptionCard(
                                title = name,
                                isSelected = customization.paletteName == name,
                                onClick = {
                                    viewModel.updateCustomization(customization.copy(paletteName = name, completedCellColor = color))
                                    scrollToPreview()
                                },
                                visual = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(color))
                                    )
                                }
                            )
                        }
                    }
                }

                StudioSection(title = "Background", icon = Icons.Default.Wallpaper) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(BackgroundType.entries) { type ->
                            StudioOptionCard(
                                title = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = customization.backgroundType == type,
                                onClick = { 
                                    viewModel.updateCustomization(customization.copy(backgroundType = type))
                                    scrollToPreview()
                                },
                                visual = { BackgroundVisualPreview(type) }
                            )
                        }
                    }
                }

                StudioSection(title = "Accent Effects", icon = Icons.Default.AutoAwesome) {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            EffectRow("Streak Glow", customization.showGlow) {
                                viewModel.updateCustomization(customization.copy(showGlow = it))
                                scrollToPreview()
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Soft Shadows", customization.showShadow) {
                                viewModel.updateCustomization(customization.copy(showShadow = it))
                                scrollToPreview()
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Highlight Today", customization.highlightToday) {
                                viewModel.updateCustomization(customization.copy(highlightToday = it))
                                scrollToPreview()
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Pulse Animation", customization.pulseEffect) {
                                viewModel.updateCustomization(customization.copy(pulseEffect = it))
                                scrollToPreview()
                            }
                        }
                    }
                }

                StudioSection(title = "Position", icon = Icons.Default.Layers) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(GridPosition.entries) { pos ->
                            StudioOptionCard(
                                title = pos.name.lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = customization.gridPosition == pos,
                                onClick = { 
                                    viewModel.updateCustomization(customization.copy(gridPosition = pos))
                                    scrollToPreview()
                                },
                                visual = { PositionVisualPreview(pos) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeVisualPreview(customization: WallpaperCustomization) {
    val bgBrush = when (customization.backgroundType) {
        BackgroundType.SOLID -> Brush.linearGradient(listOf(Color(customization.backgroundColorStart), Color(customization.backgroundColorStart)))
        else -> Brush.linearGradient(listOf(Color(customization.backgroundColorStart), Color(customization.backgroundColorEnd)))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    Color(customization.completedCellColor),
                    getShape(customization.gridShape)
                )
        )
    }
}

@Composable
fun ShapeVisualPreview(shape: GridShape, isSelected: Boolean) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(color, getShape(shape))
    )
}

@Composable
fun BackgroundVisualPreview(type: BackgroundType) {
    val brush = when (type) {
        BackgroundType.SOLID -> Brush.linearGradient(listOf(Color.Gray, Color.Gray))
        BackgroundType.GRADIENT -> Brush.linearGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2)))
        BackgroundType.PASTEL_GRADIENT -> Brush.linearGradient(listOf(Color(0xFFFFD1DC), Color(0xFFB2E2F2)))
        BackgroundType.NOISE -> Brush.linearGradient(listOf(Color.DarkGray, Color.LightGray))
        BackgroundType.GLASS_BLUR -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.1f)))
        BackgroundType.PAPER_TEXTURE -> Brush.linearGradient(listOf(Color(0xFFF5F5DC), Color(0xFFEEDC82)))
    }
    Box(modifier = Modifier.fillMaxSize().background(brush))
}

@Composable
fun LayoutVisualPreview(layout: GridLayoutType) {
    val (count, size) = when (layout) {
        GridLayoutType.COMPACT -> 4 to 4.dp
        GridLayoutType.STANDARD -> 3 to 6.dp
        GridLayoutType.SPACED -> 3 to 6.dp
        GridLayoutType.LARGE_CELLS -> 2 to 10.dp
    }
    val spacing = if (layout == GridLayoutType.SPACED) 4.dp else 2.dp

    Column(verticalArrangement = Arrangement.spacedBy(spacing), horizontalAlignment = Alignment.CenterHorizontally) {
        repeat(count - 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                repeat(count - 1) {
                    Box(Modifier.size(size).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                }
            }
        }
    }
}

@Composable
fun PositionVisualPreview(position: GridPosition) {
    Box(Modifier.fillMaxSize().padding(8.dp)) {
        Box(
            Modifier
                .size(20.dp, 12.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                .align(when (position) {
                    GridPosition.TOP -> Alignment.TopCenter
                    GridPosition.CENTER -> Alignment.Center
                    GridPosition.BOTTOM -> Alignment.BottomCenter
                })
        )
    }
}

private fun getShape(shape: GridShape): Shape {
    return when (shape) {
        GridShape.SQUARE -> RectangleShape
        GridShape.ROUNDED_SQUARE -> RoundedCornerShape(4.dp)
        GridShape.CIRCLE -> CircleShape
        GridShape.DIAMOND -> CutCornerShape(50)
        GridShape.PIXEL -> RectangleShape
        GridShape.BUBBLE -> RoundedCornerShape(8.dp)
        GridShape.DOT -> CircleShape
    }
}

@Composable
fun StudioSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }
        content()
    }
}

@Composable
fun StudioOptionCard(
    title: String, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    visual: @Composable BoxScope.() -> Unit
) {
    val borderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "BorderColor")
    val containerColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface, label = "ContainerColor")
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.width(130.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                visual()
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EffectRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            thumbContent = if (checked) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null
        )
    }
}
