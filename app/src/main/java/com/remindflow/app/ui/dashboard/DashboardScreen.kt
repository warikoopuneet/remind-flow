package com.remindflow.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindflow.app.domain.model.TimeQuadrant
import com.remindflow.app.ui.dashboard.components.CompletionRing
import com.remindflow.app.ui.dashboard.components.QuadrantCard
import com.remindflow.app.ui.dashboard.components.SnoozeSkipBottomSheet
import com.remindflow.app.ui.dashboard.components.WeekCalendarStrip
import com.remindflow.app.ui.theme.CyanAccent
import com.remindflow.app.ui.theme.IndigoPrimary
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeSnoozeTarget by viewModel.activeSnoozeTarget.collectAsState()

    val formattedHeaderDate = uiState.selectedDate.format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Remind",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Flow",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanAccent
                            )
                        }
                        Text(
                            text = formattedHeaderDate,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Horizontal Selectable Date Carousel (all dates clickable)
            item {
                WeekCalendarStrip(
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            // Daily Progress Indicator Card or Clean Slate Prompt
            if (uiState.totalCount > 0) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CompletionRing(
                            completedCount = uiState.completedCount,
                            totalCount = uiState.totalCount,
                            percentage = uiState.completionPercentage
                        )
                    }
                }
            } else {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🌱", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Clean Slate • No Reminders Scheduled",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You have no routines scheduled for this date. Tap '+' below to create your first routine!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            androidx.compose.material3.Button(
                                onClick = onNavigateToAdd,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Reminder for this Date")
                            }
                        }
                    }
                }
            }

            // 4 Quadrants: Morning, Afternoon, Evening, Night
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TimeQuadrant.entries.forEach { quadrant ->
                        val items = uiState.schedule[quadrant] ?: emptyList()
                        QuadrantCard(
                            quadrant = quadrant,
                            items = items,
                            onMarkDone = { viewModel.markDone(it) },
                            onOpenSnooze = { viewModel.openSnoozeDialog(it) },
                            onMarkSkip = { viewModel.markSkipped(it) },
                            onReset = { viewModel.resetStatus(it) },
                            onItemClick = { onNavigateToEdit(it.reminder.id) }
                        )
                    }
                }
            }
        }

        // Snooze / Skip Bottom Sheet
        SnoozeSkipBottomSheet(
            targetItem = activeSnoozeTarget,
            onDismiss = { viewModel.dismissSnoozeDialog() },
            onSnoozeMinutes = { minutes ->
                activeSnoozeTarget?.let { viewModel.markSnoozed(it, minutes) }
            },
            onSkip = {
                activeSnoozeTarget?.let {
                    viewModel.markSkipped(it)
                    viewModel.dismissSnoozeDialog()
                }
            }
        )
    }
}
