package com.dev.habitwallpaper.features.habit.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.habitwallpaper.core.designsystem.HabitColors
import com.dev.habitwallpaper.core.designsystem.toCompose
import com.dev.habitwallpaper.domain.model.Habit
import com.dev.habitwallpaper.features.habit.presentation.util.icon
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitFilter
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitsViewModel
import com.dev.habitwallpaper.features.habit.presentation.component.ConsistencyIndicatorRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel,
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    onEditHabit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        // The inner Scaffold is the sole owner of window insets for this screen.
        // WindowInsets.safeDrawing ensures the TopAppBar absorbs the status bar height
        // exactly once — the outer MainScreen Scaffold uses WindowInsets(0).
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            // CenterAlignedTopAppBar is a proper Material 3 component that internally
            // consumes WindowInsets.statusBars, so the title renders below the status
            // bar on all devices regardless of status bar height.
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Habits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = onAddHabit) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Habit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.habits.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (uiState.searchQuery.isEmpty()) "No habits yet" else "No habits match your search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        if (uiState.searchQuery.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap the + button above to create your first habit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Search + filter chips pinned at the top of the list ──
                    stickyHeader(key = "search_filter") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search habits…") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HabitFilter.entries.forEach { filter ->
                                    val isSelected = uiState.filter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onFilterChange(filter) },
                                        label = { Text(filter.displayName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            selectedBorderColor = Color.Transparent
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // ── Habit cards ─────────────────────────────────────────
                    items(uiState.habits, key = { it.id }) { habit ->
                        HabitManagementCard(
                            habit = habit,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = { onHabitClick(habit.id) },
                            onEdit = { onEditHabit(habit.id) },
                            onPause = { viewModel.togglePauseHabit(habit) },
                            onDelete = { viewModel.deleteHabit(habit.id) },
                            onSetPriority = { viewModel.setAsPriority(habit.id) }
                        )
                    }

                    // Bottom breathing room so last card clears the nav bar
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun HabitManagementCard(
    habit: Habit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onPause: () -> Unit,
    onDelete: () -> Unit,
    onSetPriority: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isPaused)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = habit.color?.let { Color(it).copy(alpha = 0.2f) }
                                    ?: MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = habit.category.icon,
                            contentDescription = null,
                            tint = habit.color?.let { Color(it) }
                                ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                habit.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (habit.isPaused)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (habit.isPaused) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.PauseCircle,
                                    contentDescription = "Paused",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (habit.isWallpaperSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Priority",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            "${habit.currentStreak} day streak • Day ${habit.currentDay} of ${habit.durationDays}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (habit.isPaused)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else
                                HabitColors.STREAK_ORANGE.toCompose(),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (habit.isPaused) "Resume" else "Pause")
                            },
                            leadingIcon = {
                                Icon(
                                    if (habit.isPaused) Icons.Default.PlayArrow
                                    else Icons.Default.Pause,
                                    contentDescription = null
                                )
                            },
                            onClick = { showMenu = false; onPause() }
                        )
                        if (!habit.isWallpaperSelected) {
                            DropdownMenuItem(
                                text = { Text("Set as Priority") },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                },
                                onClick = { showMenu = false; onSetPriority() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ConsistencyIndicatorRow(habit = habit)
        }
    }
}
