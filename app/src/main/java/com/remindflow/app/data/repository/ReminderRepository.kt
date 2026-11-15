package com.remindflow.app.data.repository

import com.remindflow.app.data.local.dao.ReminderDao
import com.remindflow.app.data.local.dao.ReminderLogDao
import com.remindflow.app.data.local.entity.ReminderEntity
import com.remindflow.app.data.local.entity.ReminderLogEntity
import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderLog
import com.remindflow.app.domain.model.ReminderStatus
import com.remindflow.app.domain.model.TimeQuadrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class ScheduledReminderItem(
    val reminder: Reminder,
    val status: ReminderStatus,
    val isLowStock: Boolean
)

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val reminderLogDao: ReminderLogDao
) {
    val allRemindersFlow: Flow<List<Reminder>> =
        reminderDao.getAllRemindersFlow().map { list -> list.map { it.toDomain() } }

    val inventoryTrackedRemindersFlow: Flow<List<Reminder>> =
        reminderDao.getInventoryTrackedRemindersFlow().map { list -> list.map { it.toDomain() } }

    val allLogsFlow: Flow<List<ReminderLog>> =
        reminderLogDao.getAllLogsFlow().map { list -> list.map { it.toDomain() } }

    /**
     * Get reminders scheduled for a specific date (day of week filter) combined with their completion status for that date.
     */
    fun getScheduleForDateFlow(dateString: String, dayOfWeek1to7: Int): Flow<Map<TimeQuadrant, List<ScheduledReminderItem>>> {
        val remindersFlow = reminderDao.getAllActiveRemindersFlow()
        val logsFlow = reminderLogDao.getLogsForDateFlow(dateString)

        return combine(remindersFlow, logsFlow) { reminders, logs ->
            val logMap = logs.associate { it.reminderId to it.toDomain() }

            val scheduledItems = reminders
                .map { it.toDomain() }
                .filter { reminder ->
                    reminder.isScheduledForDate(dateString, dayOfWeek1to7) || logMap.containsKey(reminder.id)
                }
                .map { reminder ->
                    val log = logMap[reminder.id]
                    val status = log?.status ?: ReminderStatus.PENDING
                    val isLowStock = reminder.isQuantityTracked && reminder.currentQuantity <= reminder.lowStockThreshold

                    ScheduledReminderItem(
                        reminder = reminder,
                        status = status,
                        isLowStock = isLowStock
                    )
                }

            // Group by TimeQuadrant in order: MORNING, AFTERNOON, EVENING, NIGHT
            val grouped = mutableMapOf<TimeQuadrant, List<ScheduledReminderItem>>()
            for (quadrant in TimeQuadrant.entries) {
                grouped[quadrant] = scheduledItems
                    .filter { it.reminder.quadrant == quadrant }
                    .sortedWith(compareBy({ it.reminder.hour }, { it.reminder.minute }))
            }
            grouped
        }
    }

    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    suspend fun saveReminder(reminder: Reminder): Long {
        val entity = ReminderEntity.fromDomain(reminder)
        return if (reminder.id == 0L) {
            reminderDao.insertReminder(entity)
        } else {
            reminderDao.updateReminder(entity)
            reminder.id
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(ReminderEntity.fromDomain(reminder))
    }

    suspend fun markDone(reminder: Reminder, dateString: String) {
        val log = ReminderLogEntity(
            reminderId = reminder.id,
            dateString = dateString,
            timestampMillis = System.currentTimeMillis(),
            statusName = ReminderStatus.DONE.name,
            quantityDeducted = if (reminder.isQuantityTracked) reminder.dosagePerTake else 0
        )
        reminderLogDao.insertOrUpdateLog(log)

        if (reminder.isQuantityTracked && reminder.dosagePerTake > 0) {
            reminderDao.decrementQuantity(reminder.id, reminder.dosagePerTake)
        }
    }

    suspend fun markSkipped(reminder: Reminder, dateString: String) {
        val log = ReminderLogEntity(
            reminderId = reminder.id,
            dateString = dateString,
            timestampMillis = System.currentTimeMillis(),
            statusName = ReminderStatus.SKIPPED.name,
            quantityDeducted = 0
        )
        reminderLogDao.insertOrUpdateLog(log)
    }

    suspend fun markSnoozed(reminder: Reminder, dateString: String) {
        val log = ReminderLogEntity(
            reminderId = reminder.id,
            dateString = dateString,
            timestampMillis = System.currentTimeMillis(),
            statusName = ReminderStatus.SNOOZED.name,
            quantityDeducted = 0
        )
        reminderLogDao.insertOrUpdateLog(log)
    }

    suspend fun resetStatus(reminderId: Long, dateString: String) {
        reminderLogDao.deleteLog(reminderId, dateString)
    }

    suspend fun refillStock(reminderId: Long, addedAmount: Int) {
        reminderDao.incrementQuantity(reminderId, addedAmount)
    }

    suspend fun getLog(reminderId: Long, dateString: String): ReminderLog? {
        return reminderLogDao.getLog(reminderId, dateString)?.toDomain()
    }

    suspend fun updateStock(reminderId: Long, newQuantity: Int) {
        reminderDao.updateQuantity(reminderId, newQuantity)
    }
}
