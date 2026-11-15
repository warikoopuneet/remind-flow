package com.remindflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.remindflow.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    fun getAllActiveRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    fun getAllRemindersFlow(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    fun getReminderByIdFlow(id: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE isQuantityTracked = 1 ORDER BY currentQuantity ASC")
    fun getInventoryTrackedRemindersFlow(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET currentQuantity = :newQuantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQuantity: Int)

    @Query("UPDATE reminders SET currentQuantity = MAX(0, currentQuantity - :amount) WHERE id = :id")
    suspend fun decrementQuantity(id: Long, amount: Int)

    @Query("UPDATE reminders SET currentQuantity = currentQuantity + :amount WHERE id = :id")
    suspend fun incrementQuantity(id: Long, amount: Int)
}
