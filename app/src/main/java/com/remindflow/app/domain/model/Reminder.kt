package com.remindflow.app.domain.model

enum class ScheduleMode {
    RECURRING,
    SINGLE
}

data class Reminder(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val hour: Int,
    val minute: Int,
    val quadrant: TimeQuadrant = TimeQuadrant.fromHour(hour),
    val scheduleMode: ScheduleMode = ScheduleMode.RECURRING,
    val startDate: String = "", // "YYYY-MM-DD" optional creation/start date
    val singleDate: String? = null, // "YYYY-MM-DD" for SINGLE mode
    val daysOfWeekMask: Int = 127,  // 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun (127 = all days)
    val hasEndDate: Boolean = false,
    val endDate: String? = null,    // "YYYY-MM-DD" optional end date for RECURRING mode
    val category: ReminderCategory,
    val colorHex: Long = category.colorHex,
    val iconKey: String = category.iconKey,
    val isQuantityTracked: Boolean = false,
    val currentQuantity: Int = 0,
    val dosagePerTake: Int = 1,
    val lowStockThreshold: Int = 5,
    val unitLabel: String = "items",
    val isUrgentAlarm: Boolean = false,
    val isEnabled: Boolean = true
) {
    val formattedTime: String
        get() {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format("%d:%02d %s", displayHour, minute, period)
        }

    fun isScheduledForDay(dayOfWeek1to7: Int): Boolean {
        val bit = 1 shl (dayOfWeek1to7 - 1)
        return (daysOfWeekMask and bit) != 0
    }

    fun isScheduledForDate(dateString: String, dayOfWeek1to7: Int): Boolean {
        if (!isEnabled) return false
        return if (scheduleMode == ScheduleMode.SINGLE) {
            singleDate != null && singleDate == dateString
        } else {
            if (startDate.isNotBlank() && dateString < startDate) {
                return false
            }
            if (hasEndDate && endDate != null && dateString > endDate) {
                return false
            }
            isScheduledForDay(dayOfWeek1to7)
        }
    }

    val recurrenceSummary: String
        get() {
            if (scheduleMode == ScheduleMode.SINGLE) {
                return singleDate?.let { "1-Time • $it" } ?: "1-Time"
            }
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val activeDays = (1..7).filter { isScheduledForDay(it) }.map { dayNames[it - 1] }
            val daysText = when {
                daysOfWeekMask == 127 -> "Daily"
                daysOfWeekMask == 31 -> "Mon–Fri"
                daysOfWeekMask == 96 -> "Sat–Sun"
                activeDays.isEmpty() -> "Never"
                else -> activeDays.joinToString(", ")
            }
            return if (hasEndDate && endDate != null) "$daysText (until $endDate)" else daysText
        }
}

enum class ReminderStatus {
    PENDING,
    DONE,
    SNOOZED,
    SKIPPED
}

data class ReminderLog(
    val id: Long = 0,
    val reminderId: Long,
    val dateString: String, // e.g. "2026-11-01"
    val timestampMillis: Long,
    val status: ReminderStatus,
    val quantityDeducted: Int = 0
)
