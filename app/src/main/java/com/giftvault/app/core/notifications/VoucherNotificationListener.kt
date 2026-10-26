package com.giftvault.app.core.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.giftvault.app.core.ingestion.StructuredTextParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VoucherNotificationListener : NotificationListenerService() {

    @Inject lateinit var textParser: StructuredTextParser
    @Inject lateinit var notificationHelper: NotificationHelper

    companion object {
        val WHITELISTED_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.whatsapp",
            "com.pnb.one",
            "com.inscripts.rupay"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName ?: return

        // Ignore non-whitelisted sources to eliminate battery drain
        if (pkg !in WHITELISTED_PACKAGES) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val rawPayload = "$title $body $bigText"
        val parsed = textParser.parseStructuredVoucher(rawPayload) ?: return

        if (!parsed.voucher_code.isNullOrBlank()) {
            notificationHelper.postVoucherDetectedNotification(
                merchant = parsed.merchant ?: "Gift Voucher",
                code = parsed.voucher_code,
                pin = parsed.pin,
                faceValue = parsed.face_value,
                sourcePackage = pkg
            )
        }
    }
}
