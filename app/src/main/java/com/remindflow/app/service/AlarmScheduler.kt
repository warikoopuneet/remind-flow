package com.remindflow.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder, startFromTomorrow: Boolean = false) {
        if (!reminder.isEnabled) return

        val calendar = Calendar.getInstance().apply {
            if (reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.SINGLE) {
                if (reminder.singleDate != null) {
                    val parts = reminder.singleDate.split("-").map { it.toInt() }
                    set(Calendar.YEAR, parts[0])
                    set(Calendar.MONTH, parts[1] - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2])
                }
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    // Single reminder is in the past, don't schedule
                    return
                }
            } else {
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If scheduled after completion or action for today, look strictly from tomorrow onwards
                if (startFromTomorrow) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }

                // Loop up to 14 days to find the next matching scheduled day
                var attempts = 0
                while (attempts < 14) {
                    val dayOfWeek = when (get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> 1
                        Calendar.TUESDAY -> 2
                        Calendar.WEDNESDAY -> 3
                        Calendar.THURSDAY -> 4
                        Calendar.FRIDAY -> 5
                        Calendar.SATURDAY -> 6
                        Calendar.SUNDAY -> 7
                        else -> 1
                    }

                    if (timeInMillis > System.currentTimeMillis() && reminder.isScheduledForDay(dayOfWeek)) {
                        break
                    }
                    add(Calendar.DAY_OF_YEAR, 1)
                    attempts++
                }

                // Check optional end date
                if (reminder.hasEndDate && reminder.endDate != null) {
                    val endParts = reminder.endDate.split("-").map { it.toInt() }
                    val endCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, endParts[0])
                        set(Calendar.MONTH, endParts[1] - 1)
                        set(Calendar.DAY_OF_MONTH, endParts[2])
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }
                    if (timeInMillis > endCal.timeInMillis) {
                        return // Past end date
                    }
                }
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(EXTRA_REMINDER_NOTES, reminder.notes)
            putExtra(EXTRA_REMINDER_CATEGORY, reminder.category.displayName)
            putExtra(EXTRA_REMINDER_URGENT, reminder.isUrgentAlarm)
            putExtra(EXTRA_IS_SNOOZE, false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, com.remindflow.app.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_reminder", reminder.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // AlarmManager.setAlarmClock provides exact timing down to the second (never early/batched)
            val clockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
        } catch (_: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleSnooze(reminderId: Long, title: String, notes: String, categoryName: String, snoozeMinutes: Int) {
        val triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_NOTES, "Snoozed: $notes")
            putExtra(EXTRA_REMINDER_CATEGORY, categoryName)
            putExtra(EXTRA_REMINDER_URGENT, false)
            putExtra(EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val showIntent = Intent(context, com.remindflow.app.ui.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to_reminder", reminderId)
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                (reminderId + 100000).toInt(),
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val clockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
        } catch (_: Exception) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelSnooze(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (snoozePendingIntent != null) {
            alarmManager.cancel(snoozePendingIntent)
        }
    }

    fun cancel(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
        cancelSnooze(reminderId)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_NOTES = "extra_reminder_notes"
        const val EXTRA_REMINDER_CATEGORY = "extra_reminder_category"
        const val EXTRA_REMINDER_URGENT = "extra_reminder_urgent"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}
