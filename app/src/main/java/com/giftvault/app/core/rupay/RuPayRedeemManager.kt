package com.giftvault.app.core.rupay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.giftvault.app.domain.model.BankCard
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuPayRedeemManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val RUPAY_PORTAL_URL = "https://rupayselect.truztee.com/"
        const val TRUZTEE_PAYMENT_URL_PREFIX = "https://rupayselect.truztee.com/#/pay?token="
        const val TRUZTEE_SUPPORT_EMAIL = "rupay@truztee.com"
    }

    /**
     * Launches the RuPay Select rewards portal in an isolated, sandboxed Chrome Custom Tab.
     */
    fun openRuPaySelectPortal(bankCard: BankCard? = null) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(false)
                .build()

            val intent = customTabsIntent.intent.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            customTabsIntent.launchUrl(context, Uri.parse(RUPAY_PORTAL_URL))

            if (bankCard != null) {
                Toast.makeText(
                    context,
                    "Opening RuPay Rewards for ${bankCard.cardNickname}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            // Fallback to standard browser intent if Custom Tabs fails
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(RUPAY_PORTAL_URL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    /**
     * Intercepts and launches a Truztee payment/validation link in an isolated session.
     */
    fun handleTruzteePaymentLink(paymentUrl: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(paymentUrl))
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    /**
     * Checks if a given URL is a Truztee validation / payment link.
     */
    fun isTruzteePaymentLink(url: String): Boolean {
        return url.startsWith(TRUZTEE_PAYMENT_URL_PREFIX) || url.contains("rupayselect.truztee.com/#/pay")
    }
}
