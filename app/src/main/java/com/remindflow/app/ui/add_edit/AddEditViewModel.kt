package com.remindflow.app.ui.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remindflow.app.data.repository.ReminderRepository
import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderCategory
import com.remindflow.app.domain.model.TimeQuadrant
import com.remindflow.app.service.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditUiState(
    val reminderId: Long = 0,
    val title: String = "",
    val notes: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val quadrant: TimeQuadrant = TimeQuadrant.MORNING,
    val scheduleMode: com.remindflow.app.domain.model.ScheduleMode = com.remindflow.app.domain.model.ScheduleMode.RECURRING,
    val startDate: String = java.time.LocalDate.now().toString(),
    val singleDate: String = java.time.LocalDate.now().toString(),
    val daysOfWeekMask: Int = 127, // Everyday by default
    val hasEndDate: Boolean = false,
    val endDate: String = java.time.LocalDate.now().plusMonths(1).toString(),
    val category: ReminderCategory = ReminderCategory.PERSONAL,
    val colorHex: Long = ReminderCategory.PERSONAL.colorHex,
    val iconKey: String = ReminderCategory.PERSONAL.iconKey,
    val isQuantityTracked: Boolean = false,
    val currentQuantity: String = "30",
    val dosagePerTake: String = "1",
    val lowStockThreshold: String = "5",
    val unitLabel: String = "tablets",
    val isUrgentAlarm: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddEditViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val reminderId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddEditUiState(
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            minute = 0,
            quadrant = TimeQuadrant.fromHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
            category = ReminderCategory.PERSONAL,
            isQuantityTracked = false
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (reminderId != null && reminderId > 0) {
            viewModelScope.launch {
                val existing = repository.getReminderById(reminderId)
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            reminderId = existing.id,
                            title = existing.title,
                            notes = existing.notes,
                            hour = existing.hour,
                            minute = existing.minute,
                            quadrant = TimeQuadrant.fromHour(existing.hour),
                            scheduleMode = existing.scheduleMode,
                            startDate = existing.startDate,
                            singleDate = existing.singleDate ?: java.time.LocalDate.now().toString(),
                            daysOfWeekMask = existing.daysOfWeekMask,
                            hasEndDate = existing.hasEndDate,
                            endDate = existing.endDate ?: java.time.LocalDate.now().plusMonths(1).toString(),
                            category = existing.category,
                            colorHex = existing.colorHex,
                            iconKey = existing.iconKey,
                            isQuantityTracked = existing.isQuantityTracked && existing.category.supportsInventory,
                            currentQuantity = existing.currentQuantity.toString(),
                            dosagePerTake = existing.dosagePerTake.toString(),
                            lowStockThreshold = existing.lowStockThreshold.toString(),
                            unitLabel = existing.unitLabel,
                            isUrgentAlarm = existing.isUrgentAlarm
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title, error = null) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }
    
    fun onTimeChange(hour: Int, minute: Int) = _uiState.update {
        it.copy(
            hour = hour,
            minute = minute,
            quadrant = TimeQuadrant.fromHour(hour) // Automatically derived from time
        )
    }

    fun onCategoryChange(category: ReminderCategory) = _uiState.update {
        it.copy(
            category = category,
            colorHex = category.colorHex,
            iconKey = category.iconKey,
            // Only categories supporting inventory (like Health & Meds) can have quantity tracked
            isQuantityTracked = if (category.supportsInventory) it.isQuantityTracked || true else false
        )
    }

    fun onColorChange(colorHex: Long) = _uiState.update { it.copy(colorHex = colorHex) }

    fun toggleDay(dayIndex1to7: Int) {
        _uiState.update { state ->
            val bit = 1 shl (dayIndex1to7 - 1)
            val newMask = state.daysOfWeekMask xor bit
            // Don't allow 0 days selected (at least 1 day required)
            state.copy(daysOfWeekMask = if (newMask == 0) state.daysOfWeekMask else newMask)
        }
    }

    fun setAllDays() = _uiState.update { it.copy(daysOfWeekMask = 127) }
    fun setWeekdays() = _uiState.update { it.copy(daysOfWeekMask = 31) }
    fun setWeekends() = _uiState.update { it.copy(daysOfWeekMask = 96) }

    fun onScheduleModeChange(mode: com.remindflow.app.domain.model.ScheduleMode) =
        _uiState.update { it.copy(scheduleMode = mode) }

    fun onSingleDateChange(date: String) = _uiState.update { it.copy(singleDate = date) }

    fun onHasEndDateToggle(hasEndDate: Boolean) = _uiState.update { it.copy(hasEndDate = hasEndDate) }

    fun onEndDateChange(date: String) = _uiState.update { it.copy(endDate = date) }

    fun onQuantityTrackedToggle(enabled: Boolean) = _uiState.update { it.copy(isQuantityTracked = enabled) }
    fun onCurrentQuantityChange(qty: String) = _uiState.update { it.copy(currentQuantity = qty.filter { c -> c.isDigit() }) }
    fun onDosageChange(dose: String) = _uiState.update { it.copy(dosagePerTake = dose.filter { c -> c.isDigit() }) }
    fun onThresholdChange(thresh: String) = _uiState.update { it.copy(lowStockThreshold = thresh.filter { c -> c.isDigit() }) }
    fun onUnitLabelChange(label: String) = _uiState.update { it.copy(unitLabel = label) }

    fun onUrgentToggle(isUrgent: Boolean) = _uiState.update { it.copy(isUrgentAlarm = isUrgent) }

    fun saveReminder() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a reminder title") }
            return
        }

        val todayStr = java.time.LocalDate.now().toString()
        val creationStartDate = if (currentState.reminderId > 0) {
            currentState.startDate
        } else {
            todayStr // Always set start date as today - the date of creation for new reminders
        }

        val reminder = Reminder(
            id = currentState.reminderId,
            title = currentState.title.trim(),
            notes = currentState.notes.trim(),
            hour = currentState.hour,
            minute = currentState.minute,
            quadrant = TimeQuadrant.fromHour(currentState.hour), // Strictly derived from time
            scheduleMode = currentState.scheduleMode,
            startDate = creationStartDate,
            singleDate = if (currentState.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.SINGLE) {
                currentState.singleDate.ifBlank { todayStr }
            } else null,
            daysOfWeekMask = currentState.daysOfWeekMask,
            hasEndDate = currentState.scheduleMode == com.remindflow.app.domain.model.ScheduleMode.RECURRING && currentState.hasEndDate,
            endDate = if (currentState.hasEndDate) currentState.endDate else null,
            category = currentState.category,
            colorHex = currentState.colorHex,
            iconKey = currentState.iconKey,
            isQuantityTracked = currentState.isQuantityTracked,
            currentQuantity = currentState.currentQuantity.toIntOrNull() ?: 0,
            dosagePerTake = currentState.dosagePerTake.toIntOrNull() ?: 1,
            lowStockThreshold = currentState.lowStockThreshold.toIntOrNull() ?: 5,
            unitLabel = currentState.unitLabel.ifBlank { "items" },
            isUrgentAlarm = currentState.isUrgentAlarm,
            isEnabled = true
        )

        viewModelScope.launch {
            val savedId = repository.saveReminder(reminder)
            val updatedReminder = reminder.copy(id = savedId)
            alarmScheduler.schedule(updatedReminder)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteReminder() {
        val reminderId = _uiState.value.reminderId
        if (reminderId > 0) {
            viewModelScope.launch {
                val reminder = repository.getReminderById(reminderId)
                if (reminder != null) {
                    alarmScheduler.cancel(reminderId)
                    repository.deleteReminder(reminder)
                    _uiState.update { it.copy(isSaved = true) }
                }
            }
        }
    }

    class Factory(
        private val repository: ReminderRepository,
        private val alarmScheduler: AlarmScheduler,
        private val reminderId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditViewModel(repository, alarmScheduler, reminderId) as T
        }
    }
}
