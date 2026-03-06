package com.dev.habitwallpaper.features.habit.presentation.screen

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Habit Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mandatory: Name
            item {
                OutlinedTextField(
                    value = uiState.habitName,
                    onValueChange = { viewModel.onHabitNameChange(it) },
                    label = { Text("Habit Name *") },
                    placeholder = { Text("e.g., Read for 30 mins") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    isError = uiState.error != null && uiState.habitName.isBlank()
                )
            }

            // Category Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                            shape = RoundedCornerShape(16.dp),
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

            // Mandatory: Duration
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Goal Duration *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
                                label = { Text("$days Days") },
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = if (uiState.durationDays == days && !isCustomSelected) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                        FilterChip(
                            selected = isCustomSelected,
                            onClick = { isCustomSelected = true },
                            label = { Text("Custom") },
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = if (isCustomSelected) {
                                { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    AnimatedVisibility(visible = isCustomSelected) {
                        OutlinedTextField(
                            value = customDuration,
                            onValueChange = { viewModel.onCustomDurationChange(it) },
                            label = { Text("Number of Days (max 365)") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            suffix = { Text("days") }
                        )
                    }
                }
            }

            // Mandatory: Start Date
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Start Date *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = DateUtils.formatDate(uiState.startDate),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }

            // Tracking Type
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Goal Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TrackingTypeChip(
                            selected = uiState.trackingType == TrackingType.BINARY,
                            onClick = { viewModel.onTrackingTypeChange(TrackingType.BINARY) },
                            label = "Yes/No",
                            icon = Icons.Default.CheckCircle
                        )
                        TrackingTypeChip(
                            selected = uiState.trackingType == TrackingType.NUMERIC,
                            onClick = { viewModel.onTrackingTypeChange(TrackingType.NUMERIC) },
                            label = "Target Value",
                            icon = Icons.Default.AddCircle
                        )
                    }
                    
                    AnimatedVisibility(visible = uiState.trackingType == TrackingType.NUMERIC) {
                        OutlinedTextField(
                            value = uiState.goalValue.toString().removeSuffix(".0"),
                            onValueChange = { viewModel.onGoalValueChange(it.toFloatOrNull() ?: 0f) },
                            label = { Text("Daily Target (e.g., 8 glasses, 5km)") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }

            // Reminder Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = uiState.isReminderEnabled,
                                onCheckedChange = { viewModel.onReminderEnabledChange(it) }
                            )
                        }

                        AnimatedVisibility(visible = uiState.isReminderEnabled) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    SegmentedButton(
                                        selected = uiState.isDaily,
                                        onClick = { viewModel.onReminderModeChange(true) },
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                    ) { Text("Daily") }
                                    SegmentedButton(
                                        selected = !uiState.isDaily,
                                        onClick = { viewModel.onReminderModeChange(false) },
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                    ) { Text("Custom") }
                                }

                                AnimatedVisibility(visible = !uiState.isDaily) {
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
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                                    .clickable { viewModel.toggleReminderDay(day) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedCard(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val time = uiState.reminderTime ?: LocalTime.of(9, 0)
                                        val formatter = if (is24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
                                        Text(text = "Reminder Time: ${time.format(formatter)}")
                                        Icon(Icons.Default.AccessTime, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Advanced Settings Section
            item {
                Column {
                    TextButton(
                        onClick = { showAdvanced = !showAdvanced },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Advanced Settings", fontWeight = FontWeight.SemiBold)
                            Icon(
                                if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                            // Description
                            OutlinedTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.onDescriptionChange(it) },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                minLines = 2
                            )

                            // Color Selection
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Habit Theme", style = MaterialTheme.typography.titleSmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val colors = listOf(
                                        Color(0xFF6200EE), Color(0xFF03DAC6), Color(0xFFBB86FC),
                                        Color(0xFFCF6679), Color(0xFF03A9F4), Color(0xFFFF9800)
                                    )
                                    colors.forEach { color ->
                                        val isSelected = uiState.color == color.toArgb()
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { 
                                                    viewModel.onColorChange(if (isSelected) null else color.toArgb()) 
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Wallpaper Selection
                            ListItem(
                                headlineContent = { Text("Set as Active Wallpaper") },
                                supportingContent = { Text("Show progress for this habit on your wallpaper") },
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
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

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
                }) { Text("OK") }
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
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Composable
fun TrackingTypeChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(12.dp)
    )
}
