package com.giftvault.app.core.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.giftvault.app.core.database.dao.ReminderDao
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.Reminder
import com.giftvault.app.domain.model.ReminderStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun scheduleReminder(reminder: Reminder) {
        scope.launch {
            reminderDao.insertReminder(reminder.toEntity())
            scheduleWork(reminder)
        }
    }

    private fun scheduleWork(reminder: Reminder) {
        val triggerTimeMillis = reminder.triggerDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            
        val delayMillis = triggerTimeMillis - System.currentTimeMillis()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(if (delayMillis > 0) delayMillis else 0, TimeUnit.MILLISECONDS)
            .addTag(reminder.id)
            .build()
            
        workManager.enqueueUniqueWork(
            reminder.id,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(reminderId: String) {
        scope.launch {
            workManager.cancelUniqueWork(reminderId)
            reminderDao.deleteReminderById(reminderId)
        }
    }
    
    fun cancelWorkOnly(reminderId: String) {
        workManager.cancelUniqueWork(reminderId)
    }

    fun snoozeReminder(reminder: Reminder, days: Int = 3) {
        val newTriggerDate = LocalDate.now().plusDays(days.toLong())
        val updatedReminder = reminder.copy(
            triggerDate = newTriggerDate,
            status = ReminderStatus.SNOOZED
        )
        
        scope.launch {
            reminderDao.updateReminder(updatedReminder.toEntity())
            scheduleWork(updatedReminder)
        }
    }
}
