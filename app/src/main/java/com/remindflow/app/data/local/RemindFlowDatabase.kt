package com.remindflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.remindflow.app.data.local.dao.ReminderDao
import com.remindflow.app.data.local.dao.ReminderLogDao
import com.remindflow.app.data.local.entity.ReminderEntity
import com.remindflow.app.data.local.entity.ReminderLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ReminderEntity::class, ReminderLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RemindFlowDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderLogDao(): ReminderLogDao

    companion object {
        @Volatile
        private var INSTANCE: RemindFlowDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN startDate TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): RemindFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RemindFlowDatabase::class.java,
                    "remindflow_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // In production, the app starts with a clean slate (zero default alerts).
                // Initial samples are only populated in debug builds for testing.
                if (com.remindflow.app.BuildConfig.DEBUG) {
                    INSTANCE?.let { database ->
                        scope.launch(Dispatchers.IO) {
                            populateInitialReminders(database.reminderDao())
                        }
                    }
                }
            }
        }

        /**
         * Seed sample reminders to showcase the 4 quadrants immediately to the user
         */
        suspend fun populateInitialReminders(dao: ReminderDao) {
            val samples = listOf(
                ReminderEntity(
                    title = "Morning Multivitamin",
                    notes = "Take 1 tablet with breakfast",
                    hour = 8,
                    minute = 0,
                    quadrantName = "MORNING",
                    categoryName = "HEALTH",
                    colorHex = 0xFFEC4899,
                    iconKey = "Medication",
                    isQuantityTracked = true,
                    currentQuantity = 28,
                    dosagePerTake = 1,
                    lowStockThreshold = 5,
                    unitLabel = "tablets"
                ),
                ReminderEntity(
                    title = "Hydration Goal: 500ml",
                    notes = "Drink a full glass of water before work",
                    hour = 9,
                    minute = 30,
                    quadrantName = "MORNING",
                    categoryName = "HYDRATION",
                    colorHex = 0xFF0EA5E9,
                    iconKey = "WaterDrop",
                    isQuantityTracked = false
                ),
                ReminderEntity(
                    title = "Afternoon Focus Walk",
                    notes = "15-minute quick outdoor stroll",
                    hour = 14,
                    minute = 15,
                    quadrantName = "AFTERNOON",
                    categoryName = "FITNESS",
                    colorHex = 0xFF10B981,
                    iconKey = "FitnessCenter"
                ),
                ReminderEntity(
                    title = "Review Weekly Goals",
                    notes = "Inbox zero and review task backlog",
                    hour = 16,
                    minute = 45,
                    quadrantName = "AFTERNOON",
                    categoryName = "WORK",
                    colorHex = 0xFF6366F1,
                    iconKey = "Laptop"
                ),
                ReminderEntity(
                    title = "Evening Omega-3 & Magnesium",
                    notes = "Take with dinner",
                    hour = 19,
                    minute = 30,
                    quadrantName = "EVENING",
                    categoryName = "HEALTH",
                    colorHex = 0xFF8B5CF6,
                    iconKey = "Medication",
                    isQuantityTracked = true,
                    currentQuantity = 14,
                    dosagePerTake = 2,
                    lowStockThreshold = 4,
                    unitLabel = "capsules"
                ),
                ReminderEntity(
                    title = "Wind Down & Book Reading",
                    notes = "Read at least 15 pages before sleep",
                    hour = 22,
                    minute = 15,
                    quadrantName = "NIGHT",
                    categoryName = "HABITS",
                    colorHex = 0xFF3B82F6,
                    iconKey = "SelfImprovement"
                )
            )

            samples.forEach { dao.insertReminder(it) }
        }
    }
}
