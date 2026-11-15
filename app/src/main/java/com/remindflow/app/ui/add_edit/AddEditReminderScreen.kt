package com.remindflow.app.ui.add_edit

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindflow.app.domain.model.ReminderCategory
import com.remindflow.app.domain.model.TimeQuadrant
import com.remindflow.app.ui.theme.AmberAccent
import com.remindflow.app.ui.theme.CyanAccent
import com.remindflow.app.ui.theme.EmeraldAccent
import com.remindflow.app.ui.theme.IndigoLight
import com.remindflow.app.ui.theme.IndigoPrimary
import com.remindflow.app.ui.theme.PurpleAccent
import com.remindflow.app.ui.theme.RoseAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    viewModel: AddEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            viewModel.onTimeChange(hourOfDay, minute)
        },
        uiState.hour,
        uiState.minute,
        false // 12-hour format with AM/PM
    )

    val isEditing = uiState.reminderId > 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Reminder" else "New Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveReminder() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = CyanAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title & Notes Inputs
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g., Vitamin D, Evening Jog, Read 20 mins") },
                    isError = uiState.error != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoseAccent
                    )
                }

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notes / Instructions (Optional)") },
                    placeholder = { Text("e.g., Take 2 tablets with food") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ReminderCategory.entries) { cat ->
                        val isSelected = cat == uiState.category
                        val catColor = Color(cat.colorHex)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.onCategoryChange(cat) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Color Palette Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val palettes = listOf(
                    0xFFEC4899, // Pink
                    0xFF10B981, // Emerald
                    0xFF06B6D4, // Cyan
                    0xFF6366F1, // Indigo
                    0xFF8B5CF6, // Purple
                    0xFFF59E0B, // Amber
                    0xFFF43F5E  // Rose
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palettes.forEach { hex ->
                        val color = Color(hex)
                        val isSelected = hex == uiState.colorHex

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.onColorChange(hex) }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, Color.White, CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Scheduled Time & Quadrant
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Time & Time Quadrant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val period = if (uiState.hour < 12) "AM" else "PM"
                val displayHour = when {
                    uiState.hour == 0 -> 12
                    uiState.hour > 12 -> uiState.hour - 12
                    else -> uiState.hour
                }
                val formattedTime = String.format("%d:%02d %s", displayHour, uiState.minute, period)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time Picker Trigger Chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { timePickerDialog.show() }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Pick Time",
                                tint = CyanAccent
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Scheduled Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Auto-derived Quadrant Indicator Chip (Read-only, derived automatically from scheduled time)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Quadrant (Auto-derived)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.quadrant.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(uiState.quadrant.colorHex)
                            )
                        }
                    }
                }
            }

            // Reminder Mode: Recurring vs Single (1-Time)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Reminder Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val isRecurring = uiState.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRecurring) IndigoPrimary else Color.Transparent)
                            .clickable { viewModel.onScheduleModeChange(com.remindflow.app.domain.model.ScheduleMode.RECURRING) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔁 Recurring",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecurring) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isRecurring) IndigoPrimary else Color.Transparent)
                            .clickable { viewModel.onScheduleModeChange(com.remindflow.app.domain.model.ScheduleMode.SINGLE) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📅 Single (1-Time)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (!isRecurring) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Mode 1: Single (One-Time) Mode
            if (uiState.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.SINGLE) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "One-Time Reminder Date",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = uiState.singleDate,
                            onValueChange = { viewModel.onSingleDateChange(it) },
                            label = { Text("Date (YYYY-MM-DD)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Fires only once on this specific date, then completes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Mode 2: Recurring Mode
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recurring Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isAllDays = uiState.daysOfWeekMask == 127
                        val isWeekdays = uiState.daysOfWeekMask == 31
                        val isWeekends = uiState.daysOfWeekMask == 96

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAllDays) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setAllDays() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Everyday", style = MaterialTheme.typography.labelMedium, color = if (isAllDays) Color.White else MaterialTheme.colorScheme.onSurface)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isWeekdays) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setWeekdays() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Weekdays", style = MaterialTheme.typography.labelMedium, color = if (isWeekdays) Color.White else MaterialTheme.colorScheme.onSurface)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isWeekends) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setWeekends() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Weekends", style = MaterialTheme.typography.labelMedium, color = if (isWeekends) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Day of Week Toggles (Mon - Sun)
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayNames.forEachIndexed { index, letter ->
                            val dayIndex = index + 1
                            val isDaySelected = (uiState.daysOfWeekMask and (1 shl index)) != 0

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDaySelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.toggleDay(dayIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Optional End Date Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Set End Date",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Stop repeating after this date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.hasEndDate,
                                    onCheckedChange = { viewModel.onHasEndDateToggle(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                                )
                            }

                            if (uiState.hasEndDate) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = uiState.endDate,
                                    onValueChange = { viewModel.onEndDateChange(it) },
                                    label = { Text("End Date (YYYY-MM-DD)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IndigoPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Consumables & Stock Tracking (Specifically for items like Medicines/Prescriptions that require inventory counts)
            AnimatedVisibility(visible = uiState.category.supportsInventory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track Medicine Supplies & Stock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto-decrement pill/dose count on take and get refill alerts",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isQuantityTracked,
                            onCheckedChange = { viewModel.onQuantityTrackedToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IndigoPrimary
                            )
                        )
                    }

                    AnimatedVisibility(visible = uiState.isQuantityTracked) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.currentQuantity,
                                    onValueChange = { viewModel.onCurrentQuantityChange(it) },
                                    label = { Text("Current Stock") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = uiState.dosagePerTake,
                                    onValueChange = { viewModel.onDosageChange(it) },
                                    label = { Text("Take / Dose") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.lowStockThreshold,
                                    onValueChange = { viewModel.onThresholdChange(it) },
                                    label = { Text("Refill Alert Under") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = uiState.unitLabel,
                                    onValueChange = { viewModel.onUnitLabelChange(it) },
                                    label = { Text("Unit Label") },
                                    placeholder = { Text("pills, tablets, ml") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Urgent Alarm Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Urgent Alarm",
                        tint = AmberAccent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Persistent Alarm Alert",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bypasses mute/DND with high-priority audio chime",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = uiState.isUrgentAlarm,
                    onCheckedChange = { viewModel.onUrgentToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AmberAccent
                    )
                )
            }

            // Save Reminder Button
            Button(
                onClick = { viewModel.saveReminder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Create Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Delete Button (if editing)
            if (isEditing) {
                Button(
                    onClick = { viewModel.deleteReminder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = RoseAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Reminder",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = RoseAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
