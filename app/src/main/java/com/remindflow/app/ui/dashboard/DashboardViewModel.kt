package com.remindflow.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remindflow.app.data.repository.ReminderRepository
import com.remindflow.app.data.repository.ScheduledReminderItem
import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderStatus
import com.remindflow.app.domain.model.TimeQuadrant
import com.remindflow.app.service.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val schedule: Map<TimeQuadrant, List<ScheduledReminderItem>> = emptyMap(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val completionPercentage: Float = 0f,
    val activeSnoozeTarget: ScheduledReminderItem? = null
)

class DashboardViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _activeSnoozeTarget = MutableStateFlow<ScheduledReminderItem?>(null)
    val activeSnoozeTarget: StateFlow<ScheduledReminderItem?> = _activeSnoozeTarget.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = kotlinx.coroutines.flow.combine(
        _selectedDate,
        _activeSnoozeTarget,
        _selectedDate.flatMapLatest { date ->
            repository.getScheduleForDateFlow(date.toString(), date.dayOfWeek.value)
        }
    ) { date, snoozeTarget, schedule ->
        val allItems = schedule.values.flatten()
        val total = allItems.size
        val completed = allItems.count { it.status == ReminderStatus.DONE }
        val percentage = if (total > 0) completed.toFloat() / total.toFloat() else 0f

        DashboardUiState(
            selectedDate = date,
            schedule = schedule,
            totalCount = total,
            completedCount = completed,
            completionPercentage = percentage,
            activeSnoozeTarget = snoozeTarget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun openSnoozeDialog(item: ScheduledReminderItem) {
        _activeSnoozeTarget.value = item
    }

    fun dismissSnoozeDialog() {
        _activeSnoozeTarget.value = null
    }

    fun markDone(item: ScheduledReminderItem) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.toString()
            repository.markDone(item.reminder, dateStr)
            alarmScheduler.cancelSnooze(item.reminder.id)
            if (dateStr == LocalDate.now().toString()) {
                if (item.reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                    alarmScheduler.schedule(item.reminder, startFromTomorrow = true)
                } else {
                    alarmScheduler.cancel(item.reminder.id)
                }
            }
        }
    }

    fun markSkipped(item: ScheduledReminderItem) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.toString()
            repository.markSkipped(item.reminder, dateStr)
            alarmScheduler.cancelSnooze(item.reminder.id)
            if (dateStr == LocalDate.now().toString()) {
                if (item.reminder.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING) {
                    alarmScheduler.schedule(item.reminder, startFromTomorrow = true)
                } else {
                    alarmScheduler.cancel(item.reminder.id)
                }
            }
        }
    }

    fun markSnoozed(item: ScheduledReminderItem, minutes: Int) {
        viewModelScope.launch {
            val reminder = item.reminder
            repository.markSnoozed(reminder, _selectedDate.value.toString())
            alarmScheduler.scheduleSnooze(
                reminderId = reminder.id,
                title = reminder.title,
                notes = reminder.notes,
                categoryName = reminder.category.displayName,
                snoozeMinutes = minutes
            )
            dismissSnoozeDialog()
        }
    }

    fun resetStatus(item: ScheduledReminderItem) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.toString()
            repository.resetStatus(item.reminder.id, dateStr)
            if (dateStr == LocalDate.now().toString()) {
                alarmScheduler.schedule(item.reminder, startFromTomorrow = false)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            alarmScheduler.cancel(reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    class Factory(
        private val repository: ReminderRepository,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository, alarmScheduler) as T
        }
    }
}
