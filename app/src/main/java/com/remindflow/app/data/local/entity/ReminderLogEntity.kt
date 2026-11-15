package com.remindflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.remindflow.app.domain.model.ReminderLog
import com.remindflow.app.domain.model.ReminderStatus

@Entity(
    tableName = "reminder_logs",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("reminderId"),
        Index(value = ["reminderId", "dateString"], unique = true)
    ]
)
data class ReminderLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val timestampMillis: Long,
    val statusName: String,
    val quantityDeducted: Int = 0
) {
    fun toDomain(): ReminderLog {
        return ReminderLog(
            id = id,
            reminderId = reminderId,
            dateString = dateString,
            timestampMillis = timestampMillis,
            status = try { ReminderStatus.valueOf(statusName) } catch (_: Exception) { ReminderStatus.PENDING },
            quantityDeducted = quantityDeducted
        )
    }

    companion object {
        fun fromDomain(domain: ReminderLog): ReminderLogEntity {
            return ReminderLogEntity(
                id = domain.id,
                reminderId = domain.reminderId,
                dateString = domain.dateString,
                timestampMillis = domain.timestampMillis,
                statusName = domain.status.name,
                quantityDeducted = domain.quantityDeducted
            )
        }
    }
}
