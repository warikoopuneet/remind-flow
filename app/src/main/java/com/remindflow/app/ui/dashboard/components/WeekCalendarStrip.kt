package com.remindflow.app.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindflow.app.ui.theme.IndigoLight
import com.remindflow.app.ui.theme.IndigoPrimary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekCalendarStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate dates: 7 days before today up to 30 days ahead
    val dates = remember {
        val today = LocalDate.now()
        (-7..30).map { today.plusDays(it.toLong()) }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()

            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            val dayOfMonth = date.dayOfMonth.toString()

            val backgroundColor = when {
                isSelected -> IndigoPrimary
                isToday -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }

            val textColor = when {
                isSelected -> Color.White
                else -> MaterialTheme.colorScheme.onSurface
            }

            val subtitleColor = when {
                isSelected -> Color.White.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayOfWeek.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtitleColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayOfMonth,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (isToday && !isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(IndigoLight)
                        )
                    }
                }
            }
        }
    }
}
