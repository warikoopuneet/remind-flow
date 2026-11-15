package com.remindflow.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.remindflow.app.RemindFlowApplication
import com.remindflow.app.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Dismiss notification
        notificationManager.cancel(reminderId.toInt())

        val app = context.applicationContext as RemindFlowApplication
        val repository = app.repository
        val scheduler = app.alarmScheduler
        val todayString = LocalDate.now().toString()

        when (intent.action) {
            ACTION_DONE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = repository.getReminderById(reminderId)
                    if (reminder != null) {
                        repository.markDone(reminder, todayString)
                        scheduler.cancelSnooze(reminderId)
                        if (reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                            scheduler.schedule(reminder, startFromTomorrow = true)
                        } else {
                            scheduler.cancel(reminderId)
                        }
                    }
                }
            }
            ACTION_SNOOZE -> {
                val title = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_TITLE) ?: "Reminder"
                val notes = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_NOTES) ?: ""
                val category = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_CATEGORY) ?: "General"

                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = repository.getReminderById(reminderId)
                    if (reminder != null) {
                        repository.markSnoozed(reminder, todayString)
                    }
                    scheduler.scheduleSnooze(reminderId, title, notes, category, 15)
                }
            }
            ACTION_SKIP -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = repository.getReminderById(reminderId)
                    if (reminder != null) {
                        repository.markSkipped(reminder, todayString)
                        scheduler.cancelSnooze(reminderId)
                        if (reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                            scheduler.schedule(reminder, startFromTomorrow = true)
                        } else {
                            scheduler.cancel(reminderId)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.remindflow.app.ACTION_DONE"
        const val ACTION_SNOOZE = "com.remindflow.app.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.remindflow.app.ACTION_SKIP"
        const val EXTRA_REMINDER_ID = "com.remindflow.app.EXTRA_REMINDER_ID"
    }
}
