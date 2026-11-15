package com.remindflow.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.remindflow.app.RemindFlowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val app = context.applicationContext as RemindFlowApplication
            val repository = app.repository
            val scheduler = app.alarmScheduler

            CoroutineScope(Dispatchers.IO).launch {
                val reminders = repository.allRemindersFlow.first()
                for (reminder in reminders) {
                    if (reminder.isEnabled) {
                        scheduler.schedule(reminder)
                    }
                }
            }
        }
    }
}
