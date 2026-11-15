package com.remindflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.remindflow.app.data.local.RemindFlowDatabase
import com.remindflow.app.data.repository.ReminderRepository
import com.remindflow.app.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RemindFlowApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { RemindFlowDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { ReminderRepository(database.reminderDao(), database.reminderLogDao()) }
    val alarmScheduler by lazy { AlarmScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleAllActiveReminders()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val urgentSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val reminderAudioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val urgentAudioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Routine & Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Standard scheduled alerts for your reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(reminderSoundUri, reminderAudioAttributes)
            }

            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                "Urgent & Critical Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarms that require immediate attention"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 300, 800)
                setBypassDnd(true)
                setSound(urgentSoundUri, urgentAudioAttributes)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannels(listOf(reminderChannel, urgentChannel))
        }
    }

    private fun scheduleAllActiveReminders() {
        applicationScope.launch(Dispatchers.IO) {
            val reminders = repository.allRemindersFlow.first()
            for (reminder in reminders) {
                if (reminder.isEnabled) {
                    alarmScheduler.schedule(reminder)
                }
            }
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "remindflow_reminder_channel_v2"
        const val URGENT_CHANNEL_ID = "remindflow_urgent_channel_v2"
    }
}
