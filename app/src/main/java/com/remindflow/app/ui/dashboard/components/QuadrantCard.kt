package com.remindflow.app.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindflow.app.data.repository.ScheduledReminderItem
import com.remindflow.app.domain.model.ReminderStatus
import com.remindflow.app.domain.model.TimeQuadrant
import com.remindflow.app.ui.theme.EmeraldAccent

@Composable
fun QuadrantCard(
    quadrant: TimeQuadrant,
    items: List<ScheduledReminderItem>,
    onMarkDone: (ScheduledReminderItem) -> Unit,
    onOpenSnooze: (ScheduledReminderItem) -> Unit,
    onMarkSkip: (ScheduledReminderItem) -> Unit,
    onReset: (ScheduledReminderItem) -> Unit,
    onItemClick: (ScheduledReminderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val completedCount = items.count { it.status == ReminderStatus.DONE }
    val totalCount = items.size
    val allDone = totalCount > 0 && completedCount == totalCount

    val quadrantIcon = when (quadrant) {
        TimeQuadrant.MORNING -> Icons.Default.WbSunny
        TimeQuadrant.AFTERNOON -> Icons.Default.LightMode
        TimeQuadrant.EVENING -> Icons.Default.NightsStay
        TimeQuadrant.NIGHT -> Icons.Default.Bedtime
    }

    val timeRangeText = when (quadrant) {
        TimeQuadrant.MORNING -> "06:00 – 12:00"
        TimeQuadrant.AFTERNOON -> "12:00 – 18:00"
        TimeQuadrant.EVENING -> "18:00 – 22:00"
        TimeQuadrant.NIGHT -> "22:00 – 06:00"
    }

    val quadrantAccent = Color(quadrant.colorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        // Quadrant Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(quadrantAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = quadrantIcon,
                        contentDescription = quadrant.title,
                        tint = quadrantAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = quadrant.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeRangeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (totalCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (allDone) EmeraldAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (allDone) "All Done ✓" else "$completedCount / $totalCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (allDone) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand/Collapse",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expanded List of Items in this Quadrant
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (items.isEmpty()) {
                    Text(
                        text = "No reminders in this time window",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                } else {
                    items.forEach { item ->
                        ReminderItemCard(
                            item = item,
                            onMarkDone = { onMarkDone(item) },
                            onOpenSnooze = { onOpenSnooze(item) },
                            onMarkSkip = { onMarkSkip(item) },
                            onReset = { onReset(item) },
                            onCardClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}
