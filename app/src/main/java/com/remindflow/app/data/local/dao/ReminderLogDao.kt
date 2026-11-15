package com.remindflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.remindflow.app.data.local.entity.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {
    @Query("SELECT * FROM reminder_logs WHERE dateString = :dateString")
    fun getLogsForDateFlow(dateString: String): Flow<List<ReminderLogEntity>>

    @Query("SELECT * FROM reminder_logs WHERE dateString = :dateString")
    suspend fun getLogsForDate(dateString: String): List<ReminderLogEntity>

    @Query("SELECT * FROM reminder_logs WHERE reminderId = :reminderId AND dateString = :dateString LIMIT 1")
    suspend fun getLog(reminderId: Long, dateString: String): ReminderLogEntity?

    @Query("SELECT * FROM reminder_logs ORDER BY timestampMillis DESC LIMIT 500")
    fun getAllLogsFlow(): Flow<List<ReminderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: ReminderLogEntity): Long

    @Query("DELETE FROM reminder_logs WHERE reminderId = :reminderId AND dateString = :dateString")
    suspend fun deleteLog(reminderId: Long, dateString: String)
}
