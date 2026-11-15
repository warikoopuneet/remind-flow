package com.remindflow.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remindflow.app.data.repository.ReminderRepository
import com.remindflow.app.domain.model.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val items: List<Reminder> = emptyList(),
    val lowStockCount: Int = 0
)

class InventoryViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> = repository.inventoryTrackedRemindersFlow.map { list ->
        InventoryUiState(
            items = list,
            lowStockCount = list.count { it.currentQuantity <= it.lowStockThreshold }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun refillStock(reminderId: Long, amount: Int) {
        viewModelScope.launch {
            repository.refillStock(reminderId, amount)
        }
    }

    fun updateStock(reminderId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateStock(reminderId, newQuantity)
        }
    }

    class Factory(
        private val repository: ReminderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(repository) as T
        }
    }
}
