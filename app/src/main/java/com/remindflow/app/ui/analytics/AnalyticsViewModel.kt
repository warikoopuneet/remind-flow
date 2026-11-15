package com.remindflow.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remindflow.app.data.repository.ReminderRepository
import com.remindflow.app.domain.model.ReminderLog
import com.remindflow.app.domain.model.ReminderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DayAdherence(
    val date: LocalDate,
    val completedCount: Int,
    val skippedCount: Int,
    val totalCount: Int,
    val complianceRate: Float
)

data class AnalyticsUiState(
    val overallAdherenceRate: Float = 0.92f,
    val streakDays: Int = 5,
    val totalCompleted: Int = 0,
    val totalSkipped: Int = 0,
    val recentDays: List<DayAdherence> = emptyList(),
    val recentLogs: List<ReminderLog> = emptyList()
)

class AnalyticsViewModel(
    repository: ReminderRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = repository.allLogsFlow.map { logs ->
        val completed = logs.count { it.status == ReminderStatus.DONE }
        val skipped = logs.count { it.status == ReminderStatus.SKIPPED }
        val total = completed + skipped
        val adherence = if (total > 0) completed.toFloat() / total.toFloat() else 1.0f

        // Past 7 days adherence
        val today = LocalDate.now()
        val past7Days = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateStr = date.toString()
            val dayLogs = logs.filter { it.dateString == dateStr }
            val dayDone = dayLogs.count { it.status == ReminderStatus.DONE }
            val daySkip = dayLogs.count { it.status == ReminderStatus.SKIPPED }
            val dayTotal = dayDone + daySkip
            val rate = if (dayTotal > 0) dayDone.toFloat() / dayTotal.toFloat() else 0f

            DayAdherence(
                date = date,
                completedCount = dayDone,
                skippedCount = daySkip,
                totalCount = dayTotal,
                complianceRate = rate
            )
        }

        AnalyticsUiState(
            overallAdherenceRate = adherence,
            streakDays = calculateStreak(logs),
            totalCompleted = completed,
            totalSkipped = skipped,
            recentDays = past7Days,
            recentLogs = logs.take(15)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    private fun calculateStreak(logs: List<ReminderLog>): Int {
        var streak = 0
        var checkDate = LocalDate.now()
        for (i in 0..30) {
            val dateStr = checkDate.toString()
            val hasDone = logs.any { it.dateString == dateStr && it.status == ReminderStatus.DONE }
            if (hasDone) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (i == 0) {
                // If today is not done yet, check yesterday
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak.coerceAtLeast(1)
    }

    class Factory(
        private val repository: ReminderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repository) as T
        }
    }
}
