package com.remindflow.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.remindflow.app.R
import com.remindflow.app.RemindFlowApplication
import com.remindflow.app.service.AlarmScheduler
import com.remindflow.app.ui.MainActivity

import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
        val app = context.applicationContext as RemindFlowApplication
        val repository = app.repository
        val scheduler = app.alarmScheduler
        val todayString = LocalDate.now().toString()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = repository.getReminderById(reminderId)
                if (reminder == null || !reminder.isEnabled) {
                    return@launch
                }

                // If not a snooze alarm, check if already completed or skipped for today
                if (!isSnooze) {
                    val existingLog = repository.getLog(reminderId, todayString)
                    if (existingLog != null && (existingLog.status == ReminderStatus.DONE || existingLog.status == ReminderStatus.SKIPPED)) {
                        // Already handled for today. Suppress duplicate alert and ensure next day is scheduled.
                        if (reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                            scheduler.schedule(reminder, startFromTomorrow = true)
                        }
                        return@launch
                    }
                }

                // For recurring alarms, immediately schedule the subsequent occurrence starting from tomorrow
                if (!isSnooze && reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                    scheduler.schedule(reminder, startFromTomorrow = true)
                }

                postNotificationAndPlaySound(context, reminder, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotificationAndPlaySound(context: Context, reminder: Reminder, intent: Intent) {
        val reminderId = reminder.id
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_TITLE) ?: reminder.title
        val notes = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_NOTES) ?: reminder.notes
        val category = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_CATEGORY) ?: reminder.category.displayName
        val isUrgent = intent.getBooleanExtra(AlarmScheduler.EXTRA_REMINDER_URGENT, reminder.isUrgentAlarm)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Content intent: open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_reminder", reminderId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Done
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DONE
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 15m
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmScheduler.EXTRA_REMINDER_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_REMINDER_NOTES, notes)
            putExtra(AlarmScheduler.EXTRA_REMINDER_CATEGORY, category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 3).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isUrgent) {
            RemindFlowApplication.URGENT_CHANNEL_ID
        } else {
            RemindFlowApplication.REMINDER_CHANNEL_ID
        }

        val soundUri = if (isUrgent) {
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        } else {
            android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(if (notes.isNotBlank()) notes else category)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notes.ifBlank { "Scheduled $category reminder" }))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (isUrgent) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "✓ Done", donePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "⏱ Snooze 15m", snoozePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "✕ Skip", skipPendingIntent)

        notificationManager.notify(reminderId.toInt(), builder.build())

        // Play audible ringtone alert directly so alarms always sound even on restrictive OEM battery profiles
        try {
            val ringtone = android.media.RingtoneManager.getRingtone(context, soundUri)
            if (ringtone != null) {
                ringtone.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(if (isUrgent) android.media.AudioAttributes.USAGE_ALARM else android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone.play()
            }
        } catch (_: Exception) {
            // Channel audio serves as fallback
        }
    }
}
