package com.remindflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderCategory
import com.remindflow.app.domain.model.TimeQuadrant

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val hour: Int,
    val minute: Int,
    val quadrantName: String,
    val scheduleMode: String = "RECURRING",
    val startDate: String = "",
    val singleDate: String? = null,
    val daysOfWeekMask: Int = 127,
    val hasEndDate: Boolean = false,
    val endDate: String? = null,
    val categoryName: String,
    val colorHex: Long,
    val iconKey: String,
    val isQuantityTracked: Boolean = false,
    val currentQuantity: Int = 0,
    val dosagePerTake: Int = 1,
    val lowStockThreshold: Int = 5,
    val unitLabel: String = "items",
    val isUrgentAlarm: Boolean = false,
    val isEnabled: Boolean = true
) {
    fun toDomain(): Reminder {
        val mode = try {
            com.remindflow.app.domain.model.ScheduleMode.valueOf(scheduleMode)
        } catch (_: Exception) {
            com.remindflow.app.domain.model.ScheduleMode.RECURRING
        }

        return Reminder(
            id = id,
            title = title,
            notes = notes,
            hour = hour,
            minute = minute,
            quadrant = TimeQuadrant.fromHour(hour),
            scheduleMode = mode,
            startDate = startDate,
            singleDate = singleDate,
            daysOfWeekMask = daysOfWeekMask,
            hasEndDate = hasEndDate,
            endDate = endDate,
            category = ReminderCategory.fromName(categoryName),
            colorHex = colorHex,
            iconKey = iconKey,
            isQuantityTracked = isQuantityTracked,
            currentQuantity = currentQuantity,
            dosagePerTake = dosagePerTake,
            lowStockThreshold = lowStockThreshold,
            unitLabel = unitLabel,
            isUrgentAlarm = isUrgentAlarm,
            isEnabled = isEnabled
        )
    }

    companion object {
        fun fromDomain(domain: Reminder): ReminderEntity {
            return ReminderEntity(
                id = domain.id,
                title = domain.title,
                notes = domain.notes,
                hour = domain.hour,
                minute = domain.minute,
                quadrantName = TimeQuadrant.fromHour(domain.hour).name,
                scheduleMode = domain.scheduleMode.name,
                startDate = domain.startDate,
                singleDate = domain.singleDate,
                daysOfWeekMask = domain.daysOfWeekMask,
                hasEndDate = domain.hasEndDate,
                endDate = domain.endDate,
                categoryName = domain.category.name,
                colorHex = domain.colorHex,
                iconKey = domain.iconKey,
                isQuantityTracked = domain.isQuantityTracked,
                currentQuantity = domain.currentQuantity,
                dosagePerTake = domain.dosagePerTake,
                lowStockThreshold = domain.lowStockThreshold,
                unitLabel = domain.unitLabel,
                isUrgentAlarm = domain.isUrgentAlarm,
                isEnabled = domain.isEnabled
            )
        }
    }
}
