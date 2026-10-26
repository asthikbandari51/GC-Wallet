package com.giftvault.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftvault.app.core.database.dao.ReminderDao
import com.giftvault.app.core.notifications.ReminderScheduler
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.GiftCard
import com.giftvault.app.domain.model.Reminder
import com.giftvault.app.domain.model.ReminderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiItem(val reminder: Reminder, val card: GiftCard?)

data class RemindersUiState(
    val activeReminders: List<ReminderUiItem> = emptyList(),
    val pastReminders: List<ReminderUiItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderDao: ReminderDao,
    private val cardRepository: CardRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState(isLoading = true))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderDao.getAllReminders()
                .catch { /* Handle error gracefully */ }
                .collect { entities ->
                    val activeItems = mutableListOf<ReminderUiItem>()
                    val pastItems = mutableListOf<ReminderUiItem>()
                    
                    for (entity in entities) {
                        val reminder = entity.toDomain()
                        val card = cardRepository.getCardById(reminder.cardId).firstOrNull()
                        val item = ReminderUiItem(reminder, card)
                        
                        if (reminder.status == ReminderStatus.PENDING || reminder.status == ReminderStatus.SNOOZED) {
                            activeItems.add(item)
                        } else {
                            pastItems.add(item)
                        }
                    }
                    
                    _uiState.update {
                        it.copy(
                            activeReminders = activeItems,
                            pastReminders = pastItems,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun snoozeReminder(reminder: Reminder) {
        reminderScheduler.snoozeReminder(reminder)
    }

    fun dismissReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(status = ReminderStatus.DISMISSED)
            reminderDao.updateReminder(updated.toEntity())
            reminderScheduler.cancelWorkOnly(reminder.id)
        }
    }

    fun deleteReminder(reminderId: String) {
        reminderScheduler.cancelReminder(reminderId)
    }
}
