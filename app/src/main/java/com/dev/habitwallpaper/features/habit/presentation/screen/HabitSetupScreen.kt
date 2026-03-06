package com.dev.habitwallpaper.features.habit.presentation.screen

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.habitwallpaper.core.utils.DateUtils
import com.dev.habitwallpaper.domain.model.HabitCategory
import com.dev.habitwallpaper.domain.model.TrackingType
import com.dev.habitwallpaper.features.habit.presentation.viewmodel.HabitViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitSetupScreen(
    viewModel: HabitViewModel,
    onHabitCreated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val customDuration by viewModel.customDuration.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isCustomSelected by remember { mutableStateOf(uiState.durationDays !in listOf(21, 30, 90)) }
    var showAdvanced by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onHabitCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Habit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.saveHabit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isSaving && uiState.habitName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create Habit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- HEADER ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "New Habit",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set up your habit details and schedule.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- SECTION 1: IDENTITY ---
            item {
                FormSection(title = "Identity", icon = Icons.Default.Fingerprint) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.habitName,
                            onValueChange = { viewModel.onHabitNameChange(it) },
                            label = { Text("Habit Name *") },
                            placeholder = { Text("e.g., Morning Run") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            isError = uiState.error != null && uiState.habitName.isBlank()
                        )

                        Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                        ExposedDropdownMenuBox(
                            expanded = showCategoryMenu,
                            onExpandedChange = { showCategoryMenu = !showCategoryMenu }
                        ) {
                            OutlinedTextField(
                                value = uiState.category.displayName,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(uiState.category.icon, null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                HabitCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.displayName) },
                                        leadingIcon = { Icon(category.icon, null) },
                                        onClick = {
                                            viewModel.onCategoryChange(category)
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: GOAL & PROGRESS ---
            item {
                FormSection(title = "Goal & Progress", icon = Icons.Default.Flag) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Target Duration *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presetDurations = listOf(21, 30, 90)
                            presetDurations.forEach { days ->
                                FilterChip(
                                    selected = uiState.durationDays == days && !isCustomSelected,
                                    onClick = { 
                                        isCustomSelected = false
                                        viewModel.onDurationChange(days) 
                                    },
                                    label = { Text("$days Days") }
                                )
                            }
                            FilterChip(
                                selected = isCustomSelected,
                                onClick = { isCustomSelected = true },
                                label = { Text("Custom") }
                            )
                        }

                        AnimatedVisibility(visible = isCustomSelected) {
                            OutlinedTextField(
                                value = customDuration,
                                onValueChange = { viewModel.onCustomDurationChange(it) },
                                label = { Text("Number of Days (1-365)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                suffix = { Text("days") }
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Start Date
                            OutlinedCard(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Start Date", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Event, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp), 
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = DateUtils.formatDate(uiState.startDate), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            // Tracking Type
                            OutlinedCard(
                                onClick = { 
                                    val nextType = if(uiState.trackingType == TrackingType.BINARY) TrackingType.NUMERIC else TrackingType.BINARY
                                    viewModel.onTrackingTypeChange(nextType) 
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Tracking Type", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if(uiState.trackingType == TrackingType.BINARY) Icons.Default.CheckCircle else Icons.Default.AddChart,
                                            contentDescription = null, 
                                            modifier = Modifier.size(16.dp), 
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if(uiState.trackingType == TrackingType.BINARY) "Yes/No" else "Numeric", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        if (uiState.trackingType == TrackingType.NUMERIC) {
                            OutlinedTextField(
                                value = uiState.goalValue.toString().removeSuffix(".0"),
                                onValueChange = { viewModel.onGoalValueChange(it.toFloatOrNull() ?: 1f) },
                                label = { Text("Daily Target Value") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            // --- SECTION 3: REMINDERS ---
            item {
                FormSection(title = "Reminders", icon = Icons.Default.NotificationsActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = uiState.isReminderEnabled,
                                onCheckedChange = { viewModel.onReminderEnabledChange(it) }
                            )
                        }

                        AnimatedVisibility(visible = uiState.isReminderEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedCard(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            val time = uiState.reminderTime ?: LocalTime.of(9, 0)
                                            val formatter = if (is24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
                                            Text(text = time.format(formatter), style = MaterialTheme.typography.titleMedium)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    DayOfWeek.entries.forEach { day ->
                                        val isSelected = uiState.reminderDays.contains(day)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { viewModel.toggleReminderDay(day) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: ADVANCED ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    TextButton(
                        onClick = { showAdvanced = !showAdvanced },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Advanced Settings", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Icon(imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                        }
                    }

                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.onDescriptionChange(it) },
                                label = { Text("Habit Description") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Habit Theme Color", style = MaterialTheme.typography.labelLarge)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val colors = listOf(
                                        Color(0xFF1B4332), Color(0xFF52B788), Color(0xFFD68C45),
                                        Color(0xFF2D6A4F), Color(0xFF1A73E8), Color(0xFFD93025)
                                    )
                                    colors.forEach { color ->
                                        val isSelected = uiState.color == color.toArgb()
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                .clickable { viewModel.onColorChange(if (isSelected) null else color.toArgb()) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) Icon(
                                                imageVector = Icons.Default.Check, 
                                                contentDescription = null, 
                                                modifier = Modifier.size(20.dp), 
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            ListItem(
                                headlineContent = { Text("Active Wallpaper habit") },
                                supportingContent = { Text("Prioritize this habit's progress on the home screen.") },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.isWallpaperSelected,
                                        onCheckedChange = { viewModel.onWallpaperSelectedChange(it) }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.padding(horizontal = 0.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onStartDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val initialTime = uiState.reminderTime ?: LocalTime.of(9, 0)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24Hour
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onReminderTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("Set Time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        content()
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
