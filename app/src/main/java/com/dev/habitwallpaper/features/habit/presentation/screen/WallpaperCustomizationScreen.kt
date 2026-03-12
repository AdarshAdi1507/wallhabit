package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.habitwallpaper.domain.model.*
import com.dev.habitwallpaper.features.habit.presentation.component.WallpaperPreview
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.WallpaperCustomizationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCustomizationScreen(
    onBack: () -> Unit,
    viewModel: WallpaperCustomizationViewModel = hiltViewModel()
) {
    val customization by viewModel.customization.collectAsState()
    val previewState by viewModel.previewState.collectAsState()

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
                    IconButton(onClick = { viewModel.resetToDefault() }) {
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
                        onClick = { viewModel.resetToDefault() },
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { 
                            viewModel.applyWallpaper()
                            onBack() // Navigate back after applying
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
                .verticalScroll(rememberScrollState())
        ) {
            // Live Preview Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Device Frame Decoration
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
                // Themes Section
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
                                onClick = { viewModel.updateCustomization(preset) }
                            )
                        }
                    }
                }

                // Grid Shape Section
                StudioSection(title = "Grid Shape", icon = Icons.Default.Category) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(GridShape.entries) { shape ->
                            StudioOptionCard(
                                title = shape.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = customization.gridShape == shape,
                                onClick = { viewModel.updateCustomization(customization.copy(gridShape = shape)) }
                            )
                        }
                    }
                }

                // Grid Layout Section
                StudioSection(title = "Grid Layout", icon = Icons.Default.GridView) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GridLayoutType.entries.forEach { layout ->
                                FilterChip(
                                    selected = customization.gridLayoutType == layout,
                                    onClick = { viewModel.updateCustomization(customization.copy(gridLayoutType = layout)) },
                                    label = { Text(layout.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Spacing", 
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))
                            Slider(
                                value = customization.gridSpacing,
                                onValueChange = { viewModel.updateCustomization(customization.copy(gridSpacing = it)) },
                                valueRange = 2f..30f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Color Palette Section
                StudioSection(title = "Color Palette", icon = Icons.Default.ColorLens) {
                    PaletteSelector(
                        selectedPalette = customization.paletteName,
                        onPaletteSelected = { name, color ->
                            viewModel.updateCustomization(customization.copy(paletteName = name, completedCellColor = color))
                        }
                    )
                }

                // Background Section
                StudioSection(title = "Background", icon = Icons.Default.Wallpaper) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(BackgroundType.entries) { type ->
                            StudioOptionCard(
                                title = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                isSelected = customization.backgroundType == type,
                                onClick = { viewModel.updateCustomization(customization.copy(backgroundType = type)) }
                            )
                        }
                    }
                }

                // Accent Effects Section
                StudioSection(title = "Accent Effects", icon = Icons.Default.AutoAwesome) {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            EffectRow("Streak Glow", customization.showGlow) {
                                viewModel.updateCustomization(customization.copy(showGlow = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Soft Shadows", customization.showShadow) {
                                viewModel.updateCustomization(customization.copy(showShadow = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Highlight Today", customization.highlightToday) {
                                viewModel.updateCustomization(customization.copy(highlightToday = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            EffectRow("Pulse Animation", customization.pulseEffect) {
                                viewModel.updateCustomization(customization.copy(pulseEffect = it))
                            }
                        }
                    }
                }

                // Position Section
                StudioSection(title = "Position", icon = Icons.Default.Layers) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GridPosition.entries.forEach { pos ->
                            val isSelected = customization.gridPosition == pos
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.updateCustomization(customization.copy(gridPosition = pos)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    pos.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
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
fun StudioOptionCard(title: String, isSelected: Boolean, onClick: () -> Unit) {
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
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
fun PaletteSelector(selectedPalette: String, onPaletteSelected: (String, Int) -> Unit) {
    val palettes = listOf(
        "Matcha Green" to 0xFF4CAF50.toInt(),
        "Mint Fresh" to 0xFF58D68D.toInt(),
        "Emerald Focus" to 0xFF00C853.toInt(),
        "Soft Pastel" to 0xFFFF80AB.toInt(),
        "Forest Calm" to 0xFF2E7D32.toInt(),
        "Dark Neon" to 0xFFBB86FC.toInt()
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(palettes) { (name, color) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onPaletteSelected(name, color) }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(
                            if (selectedPalette == name) 3.dp else 0.dp,
                            if (selectedPalette == name) MaterialTheme.colorScheme.primary else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPalette == name) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    name, 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedPalette == name) FontWeight.Bold else FontWeight.Medium
                )
            }
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