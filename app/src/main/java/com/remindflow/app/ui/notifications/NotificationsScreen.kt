package com.remindflow.app.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remindflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullscreenAlarm by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf("Gentle Chime") }
    var activeAlertDismissed by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val tones = listOf("Gentle Chime", "Marimba", "Radar Alert", "Zen Bowl")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications & Alarms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Active Due Alert
                item {
                    Text(
                        text = "Active Alert (Due Now)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (!activeAlertDismissed) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SurfaceContainerDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Notification App Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "⏰", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "RemindFlow • Just now",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        color = RoseAccent.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "High Priority",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RoseAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Omega-3 & Magnesium",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Take 2 capsules with dinner • Evening Quadrant",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = AmberAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Low Stock: 4 capsules left",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            activeAlertDismissed = true
                                            toastMessage = "✓ Marked Done from notification card"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Text(text = "✓ Done / Take", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            activeAlertDismissed = true
                                            toastMessage = "⏱ Snoozed 15 mins"
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.1f)
                                    ) {
                                        Text(text = "⏱ Snooze", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            activeAlertDismissed = true
                                            toastMessage = "✕ Skipped for today"
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(0.9f)
                                    ) {
                                        Text(text = "✕ Skip", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "All active notifications handled!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = { activeAlertDismissed = false }) {
                                    Text("Reset Alert")
                                }
                            }
                        }
                    }
                }

                // Full-Screen Alarm Mode Preview Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚨 Full-Screen Alarm Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Persistent ringing screen for critical routines & medicines",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showFullscreenAlarm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Preview Alarm", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Section: Notification Channels
                item {
                    Text(
                        text = "Android Notification Channels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ChannelRow(
                                title = "Standard Routine Chimes",
                                subtitle = "Heads-up banner for workouts, chores & tasks",
                                tag = "Sound & Vibrate",
                                isUrgent = false
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ChannelRow(
                                title = "Urgent Medication Alarms",
                                subtitle = "Overrides DND / silent mode until taken",
                                tag = "High Urgency",
                                isUrgent = true
                            )
                        }
                    }
                }

                // Section: Sound & Tone
                item {
                    Text(
                        text = "Ringtone & Audio Chime",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tones.forEach { tone ->
                            val isSelected = tone == selectedTone
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTone = tone }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = tone,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Recent Log
                item {
                    Text(
                        text = "Recent Notification History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HistoryItem(
                            icon = "✓",
                            title = "Multivitamin & Zinc",
                            desc = "Action: [✓ Done] taken from notification bar • 08:00 AM",
                            color = EmeraldAccent
                        )
                        HistoryItem(
                            icon = "✓",
                            title = "Morning Hydration Goal",
                            desc = "Action: [✓ Done] completed • 09:30 AM",
                            color = EmeraldAccent
                        )
                        HistoryItem(
                            icon = "⏱",
                            title = "Afternoon Focus Walk",
                            desc = "Action: [⏱ Snooze 15m] pushed • 02:15 PM",
                            color = AmberAccent
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }

            // Toast overlay
            toastMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { toastMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    }

    // Fullscreen Alarm Simulation Dialog
    if (showFullscreenAlarm) {
        Dialog(
            onDismissRequest = { showFullscreenAlarm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing Background Ring
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(IndigoPrimary.copy(alpha = 0.15f))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 48.dp)
                ) {
                    // Top Alarm Time
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "07:30 PM",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Surface(
                            color = RoseAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent)
                        ) {
                            Text(
                                text = "🔊 PERSISTENT ALARM RINGING",
                                color = RoseAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Center Medicine/Routine Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PurpleAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "💊", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Omega-3 & Magnesium",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Take 2 capsules with dinner • 4 capsules left",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showFullscreenAlarm = false
                                toastMessage = "⏱ Alarm Snoozed for 10 minutes"
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("⏱ Snooze 10m", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showFullscreenAlarm = false
                                activeAlertDismissed = true
                                toastMessage = "✓ Marked Taken / Done!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("✓ Take Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    title: String,
    subtitle: String,
    tag: String,
    isUrgent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = if (isUrgent) RoseAccent.copy(alpha = 0.15f) else IndigoPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUrgent) RoseAccent else IndigoPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun HistoryItem(
    icon: String,
    title: String,
    desc: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
