package com.giftvault.app.core.ingestion

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Immutable
import com.giftvault.app.core.parser.GiftCardTextParser
import com.giftvault.app.domain.model.GiftCardParseResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class DetectedVoucherBannerState(
    val parseResult: GiftCardParseResult,
    val displayText: String,
    val merchantName: String,
    val amount: Double?,
    val code: String
)

@Singleton
class SmartIngestionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val textParser: GiftCardTextParser
) {
    private val _detectedVoucher = MutableStateFlow<DetectedVoucherBannerState?>(null)
    val detectedVoucher: StateFlow<DetectedVoucherBannerState?> = _detectedVoucher.asStateFlow()

    private var lastCheckedClipText: String = ""

    /**
     * Checks system clipboard for newly copied gift vouchers/SMS.
     */
    fun checkClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()?.trim() ?: ""
                if (text.isNotBlank() && text != lastCheckedClipText) {
                    lastCheckedClipText = text
                    inspectAndDetect(text)
                } else if (text.isBlank()) {
                    _detectedVoucher.value = null
                }
            } else {
                _detectedVoucher.value = null
            }
        } catch (_: Exception) {
            // Ignore clipboard access security exceptions
        }
    }

    /**
     * Inspects raw text (from SMS, push notification or manual copy).
     */
    fun inspectAndDetect(rawText: String) {
        if (rawText.isBlank()) {
            _detectedVoucher.value = null
            return
        }

        val result = textParser.parseText(rawText)
        val code = result.cardCode

        // Valid voucher detection condition: Has a detected code of length >= 8 and (either merchant or amount detected)
        if (!code.isNullOrBlank() && code.length >= 8 && (result.merchantName != null || result.amount != null)) {
            val merchant = result.merchantName ?: "Coupon"
            val amtText = if (result.amount != null && result.amount > 0) "₹${result.amount.toInt()}" else ""
            val detail = if (amtText.isNotBlank()) "$merchant • $amtText" else "$merchant • ••${code.takeLast(4)}"

            _detectedVoucher.value = DetectedVoucherBannerState(
                parseResult = result,
                displayText = detail,
                merchantName = merchant,
                amount = result.amount,
                code = code
            )
        } else {
            _detectedVoucher.value = null
        }
    }

    fun dismissBanner() {
        _detectedVoucher.value = null
    }
}
