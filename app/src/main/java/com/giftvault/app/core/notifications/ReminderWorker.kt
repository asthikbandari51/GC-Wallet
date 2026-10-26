package com.giftvault.app.core.notifications

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.giftvault.app.core.database.GiftVaultDatabase
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.ReminderStatus

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = Room.databaseBuilder(
            applicationContext,
            GiftVaultDatabase::class.java,
            GiftVaultDatabase.DATABASE_NAME
        ).build()

        val reminderDao = db.reminderDao()
        val cardDao = db.giftCardDao()
        val notificationHelper = NotificationHelper(applicationContext)

        val currentTime = System.currentTimeMillis()
        val remindersToTrigger = reminderDao.getRemindersToTrigger(currentTime)

        for (entity in remindersToTrigger) {
            val reminder = entity.toDomain()
            val cardEntity = cardDao.getCardByIdDirect(reminder.cardId)
            if (cardEntity != null) {
                val daysText = "${reminder.windowDays} days" 
                notificationHelper.showExpiryNotification(
                    cardId = cardEntity.id,
                    merchantName = cardEntity.merchantName,
                    expiryDaysText = daysText
                )
                
                val updatedReminder = reminder.copy(status = ReminderStatus.SENT)
                reminderDao.updateReminder(updatedReminder.toEntity())
            }
        }
        
        return Result.success()
    }
}
