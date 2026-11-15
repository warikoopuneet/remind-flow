package com.remindflow.app.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindflow.app.data.repository.ScheduledReminderItem
import com.remindflow.app.domain.model.ReminderStatus
import com.remindflow.app.ui.theme.AmberAccent
import com.remindflow.app.ui.theme.EmeraldAccent
import com.remindflow.app.ui.theme.RoseAccent

@Composable
fun ReminderItemCard(
    item: ScheduledReminderItem,
    onMarkDone: () -> Unit,
    onOpenSnooze: () -> Unit,
    onMarkSkip: () -> Unit,
    onReset: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminder = item.reminder
    val status = item.status
    val isDone = status == ReminderStatus.DONE
    val isSkipped = status == ReminderStatus.SKIPPED
    val isSnoozed = status == ReminderStatus.SNOOZED

    val cardBg by animateColorAsState(
        targetValue = when {
            isDone -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isSkipped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "card_bg"
    )

    val itemColor = Color(reminder.colorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable { onCardClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Color Indicator Strip / Icon Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(itemColor.copy(alpha = if (isDone) 0.2f else 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.title.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reminder.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else itemColor,
                        fontWeight = FontWeight.Bold
                    )

                    // Low Stock Alert Badge
                    if (item.isLowStock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RoseAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Low Stock",
                                tint = RoseAccent,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Only ${reminder.currentQuantity} left",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = RoseAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (reminder.isQuantityTracked) {
                        Text(
                            text = "• ${reminder.currentQuantity} ${reminder.unitLabel}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (reminder.notes.isNotBlank()) {
                    Text(
                        text = reminder.notes,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status chip if Snoozed or Skipped
                if (isSnoozed) {
                    Text(
                        text = "⏱ Snoozed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isSkipped) {
                    Text(
                        text = "✕ Skipped today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isDone) {
                    // Undo Done
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldAccent)
                            .clickable { onReset() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed - tap to undo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Snooze Action Button
                    IconButton(
                        onClick = onOpenSnooze,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Snooze",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Skip Action Button
                    IconButton(
                        onClick = onMarkSkip,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Done / Take Checkmark Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onMarkDone() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark Done",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
