package com.giftvault.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.giftvault.app.MainActivity
import com.giftvault.app.R
import com.giftvault.app.core.theme.ThemeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager? = null
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Thread-safe notification deduplication cache (key -> timestamp)
    private val recentNotificationTimestamps = ConcurrentHashMap<String, Long>()

    init {
        createAllNotificationChannels()
    }

    fun createAllNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val expiryChannel = NotificationChannel(
                CHANNEL_EXPIRY_ALERTS,
                "Voucher Expiry Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts when saved gift vouchers are nearing expiration"
                enableVibration(true)
            }

            val ingestionChannel = NotificationChannel(
                CHANNEL_VOUCHER_DETECTED,
                "New Voucher Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alert when new reward vouchers are received via SMS or WhatsApp"
                enableVibration(true)
            }

            val perkChannel = NotificationChannel(
                CHANNEL_PERK_CYCLE,
                "RuPay & Card Perk Cycles",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Quarterly and annual cycle reminders for cardholder lounge, spa, and OTT quotas"
                enableVibration(true)
            }

            val backupChannel = NotificationChannel(
                CHANNEL_SYSTEM_BACKUP,
                "Vault Backups & Security",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status alerts for automated encrypted vault backups"
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(expiryChannel, ingestionChannel, perkChannel, backupChannel)
            )
        }
    }

    /**
     * Checks if a notification with this deduplication key was recently posted.
     * Prevents repeated spam alerts within a 5-minute window.
     */
    private fun isDuplicate(dedupeKey: String, windowMillis: Long = 300_000L): Boolean {
        val now = System.currentTimeMillis()
        val lastSent = recentNotificationTimestamps[dedupeKey]
        if (lastSent != null && (now - lastSent) < windowMillis) {
            return true
        }
        recentNotificationTimestamps[dedupeKey] = now
        // Clean up old entries
        if (recentNotificationTimestamps.size > 100) {
            recentNotificationTimestamps.entries.removeIf { (now - it.value) > windowMillis }
        }
        return false
    }

    /**
     * Posts a heads-up notification when a new voucher is detected via notification/SMS ingestion.
     */
    fun postVoucherDetectedNotification(
        merchant: String,
        code: String,
        pin: String?,
        faceValue: Double?,
        sourcePackage: String = ""
    ) {
        createAllNotificationChannels()

        val dedupeKey = "INGEST:$sourcePackage:$merchant:${code.takeLast(4)}:${faceValue ?: 0.0}"
        if (isDuplicate(dedupeKey)) return

        val isPrivacyMode = themeManager?.isPrivacyModeEnabled?.value == true

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.giftvault.ACTION_INGEST_VOUCHER"
            putExtra("EXTRA_MERCHANT", merchant)
            putExtra("EXTRA_CODE", code)
            putExtra("EXTRA_PIN", pin)
            putExtra("EXTRA_AMOUNT", faceValue ?: 0.0)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("INGEST_" + merchant + code).hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎁 New Voucher Detected"
        val contentText = if (isPrivacyMode) {
            "A new reward voucher was detected. Tap to add to Vault."
        } else {
            val amountStr = if (faceValue != null && faceValue > 0) "₹${faceValue.toInt()} " else ""
            "$amountStr$merchant voucher detected. Tap to add to Vault."
        }

        val smallIcon = try {
            R.drawable.logo
        } catch (e: Exception) {
            android.R.drawable.ic_popup_reminder
        }

        val notificationId = ("VOUCHER_" + merchant + code).hashCode()

        val builder = NotificationCompat.Builder(context, CHANNEL_VOUCHER_DETECTED)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.logo, "Add to Vault", pendingIntent)
            .setGroup(GROUP_VOUCHERS)
            .setAutoCancel(true)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: Exception) {}
    }

    /**
     * Posts an expiry alert notification for vouchers nearing expiration.
     */
    fun showExpiryNotification(
        cardId: String,
        merchantName: String,
        expiryDaysText: String
    ) {
        createAllNotificationChannels()

        val dedupeKey = "EXPIRY:$cardId:$expiryDaysText"
        if (isDuplicate(dedupeKey, windowMillis = 86_400_000L)) return // Once per day per card

        val isPrivacyMode = themeManager?.isPrivacyModeEnabled?.value == true

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_CARD_ID", cardId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            cardId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = try {
            R.drawable.logo
        } catch (e: Exception) {
            android.R.drawable.ic_popup_reminder
        }

        val title = "⚠️ Voucher Expiring Soon"
        val contentText = if (isPrivacyMode) {
            "A voucher in your vault expires in $expiryDaysText"
        } else {
            "$merchantName voucher expires in $expiryDaysText"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_EXPIRY_ALERTS)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.logo, "View Voucher", pendingIntent)
            .setGroup(GROUP_EXPIRY)
            .setAutoCancel(true)

        try {
            notificationManager.notify(cardId.hashCode(), builder.build())
        } catch (_: Exception) {}
    }

    /**
     * Posts a RuPay perk cycle reset reminder.
     */
    fun showPerkCycleExpiryNotification(
        cycleName: String,
        daysRemaining: Int,
        targetDateText: String
    ) {
        createAllNotificationChannels()

        val dedupeKey = "PERK_CYCLE:$cycleName:$daysRemaining"
        if (isDuplicate(dedupeKey, windowMillis = 43_200_000L)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("PERK_CYCLE_" + cycleName).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = try {
            R.drawable.logo
        } catch (e: Exception) {
            android.R.drawable.ic_popup_reminder
        }

        val title = if (daysRemaining == 1) {
            "⚡ Last Day: RuPay Perk Cycle Ending Tomorrow!"
        } else {
            "⏰ RuPay Perk Cycle Ends in $daysRemaining Days!"
        }

        val message = "Your $cycleName perks (Spa, Golf, Lounge, OTT) reset after $targetDateText. Claim before they lapse!"

        val builder = NotificationCompat.Builder(context, CHANNEL_PERK_CYCLE)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.logo, "View Perks", pendingIntent)
            .setGroup(GROUP_PERKS)
            .setAutoCancel(true)

        try {
            notificationManager.notify(("PERK_" + cycleName + "_" + daysRemaining).hashCode(), builder.build())
        } catch (_: Exception) {}
    }

    companion object {
        const val CHANNEL_EXPIRY_ALERTS = "giftvault_expiry_alerts"
        const val CHANNEL_VOUCHER_DETECTED = "giftvault_voucher_ingestion"
        const val CHANNEL_PERK_CYCLE = "giftvault_perk_cycle"
        const val CHANNEL_SYSTEM_BACKUP = "giftvault_system_backup"

        const val GROUP_VOUCHERS = "com.giftvault.GROUP_VOUCHERS"
        const val GROUP_EXPIRY = "com.giftvault.GROUP_EXPIRY"
        const val GROUP_PERKS = "com.giftvault.GROUP_PERKS"
    }
}
