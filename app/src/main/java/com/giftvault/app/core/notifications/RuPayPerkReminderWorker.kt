package com.giftvault.app.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class RuPayPerkReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RuPayPerkEntryPoint {
        fun notificationHelper(): NotificationHelper
    }

    companion object {
        const val WORK_NAME = "RuPayPerkCycleExpiryChecker"

        fun schedulePerkReminders(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Run check every 12 hours
            val checkRequest = PeriodicWorkRequestBuilder<RuPayPerkReminderWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                checkRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationHelper = EntryPointAccessors.fromApplication(
                context,
                RuPayPerkEntryPoint::class.java
            ).notificationHelper()

            val today = LocalDate.now()
            val year = today.year

            // Define Perk Reset Boundaries
            data class PerkBoundary(
                val name: String,
                val date: LocalDate
            )

            val boundaries = listOf(
                PerkBoundary("Q1 (Jan - Mar)", LocalDate.of(year, Month.MARCH, 31)),
                PerkBoundary("Q2 / H1 (Apr - Jun)", LocalDate.of(year, Month.JUNE, 30)),
                PerkBoundary("Q3 (Jul - Sep)", LocalDate.of(year, Month.SEPTEMBER, 30)),
                PerkBoundary("Q4 / H2 / Annual (Oct - Dec)", LocalDate.of(year, Month.DECEMBER, 31))
            )

            val prefs = context.getSharedPreferences("giftvault_perk_notifications", Context.MODE_PRIVATE)
            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

            for (boundary in boundaries) {
                val daysUntil = ChronoUnit.DAYS.between(today, boundary.date).toInt()

                // Check 14 days, 7 days, and 1 day prior
                if (daysUntil in listOf(14, 7, 1)) {
                    val notifKey = "notif_${boundary.date}_${daysUntil}"
                    val alreadySent = prefs.getBoolean(notifKey, false)

                    if (!alreadySent) {
                        notificationHelper.showPerkCycleExpiryNotification(
                            cycleName = boundary.name,
                            daysRemaining = daysUntil,
                            targetDateText = boundary.date.format(dateFormatter)
                        )
                        prefs.edit().putBoolean(notifKey, true).apply()
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
