package com.giftvault.app

import android.app.Application
import com.giftvault.app.core.backup.AutoBackupWorker
import com.giftvault.app.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class GiftVaultApp : Application() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationEntryPoint {
        fun notificationHelper(): NotificationHelper
    }

    override fun onCreate() {
        super.onCreate()
        // Eagerly initialize NotificationHelper
        EntryPointAccessors
            .fromApplication(this, NotificationEntryPoint::class.java)
            .notificationHelper()

        // Schedule Rolling Auto-Backup (v0.9 Module 8)
        try {
            AutoBackupWorker.scheduleRollingBackup(this)
            com.giftvault.app.core.notifications.RuPayPerkReminderWorker.schedulePerkReminders(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
