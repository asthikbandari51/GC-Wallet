package com.giftvault.app.core.backup

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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AutoBackupEntryPoint {
        fun backupManager(): BackupManager
    }

    companion object {
        const val WORK_NAME = "GiftVaultRollingBackup"
        const val MAX_SNAPSHOTS = 3

        fun scheduleRollingBackup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()

            val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupManager = EntryPointAccessors.fromApplication(
                context,
                AutoBackupEntryPoint::class.java
            ).backupManager()

            val backupDir = File(context.filesDir, "rolling_backups").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val targetFile = File(backupDir, "giftvault_backup_$timeStamp.giftvault")

            // Default internal auto-backup password
            val internalKey = "GiftVault_Rolling_AutoBackup_Key"
            val uri = android.net.Uri.fromFile(targetFile)

            val exportResult = backupManager.exportBackup(internalKey, uri)
            if (exportResult.isFailure) {
                return@withContext Result.retry()
            }

            // FIFO Pruning: Keep last MAX_SNAPSHOTS, delete older files
            pruneOldSnapshots(backupDir)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun pruneOldSnapshots(dir: File) {
        val files = dir.listFiles { _, name -> name.startsWith("giftvault_backup_") && name.endsWith(".giftvault") }
            ?: return

        if (files.size > MAX_SNAPSHOTS) {
            val sorted = files.sortedByDescending { it.lastModified() }
            val toDelete = sorted.drop(MAX_SNAPSHOTS)
            for (f in toDelete) {
                f.delete()
            }
        }
    }
}
