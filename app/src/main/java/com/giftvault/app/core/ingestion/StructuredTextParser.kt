package com.giftvault.app.core.ingestion

import com.giftvault.app.core.templates.TemplateRegistry
import kotlinx.serialization.Serializable
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExtractedVoucherModel(
    val merchant: String?,
    val voucher_code: String?,
    val pin: String?,
    val face_value: Double?,
    val currency: String = "INR",
    val expiry_date: String?,
    val is_subscription: Boolean = false
)

@Singleton
class StructuredTextParser @Inject constructor() {

    companion object {
        // Alphanumeric card code regex (8 to 24 characters)
        private val CARD_CODE_REGEX = Pattern.compile("(?i)(?:voucher\\s*code|card\\s*number|card\\s*no|code|voucher\\s*no|voucher|coupon\\s*code)[\\s:=-]*([A-Z0-9]{8,24})")
        private val RAW_CODE_REGEX = Pattern.compile("\\b([A-Z0-9]{12,19})\\b")

        // PIN patterns
        private val PIN_REGEX = Pattern.compile("(?i)(?:pin|security\\s*pin|cvv|passcode|secret)[\\s:=-]*([0-9]{3,8})")

        // Monetary amount patterns
        private val AMOUNT_REGEX = Pattern.compile("(?i)(?:rs\\.?|inr|₹|worth|amount|value)[\\s:=-]*([\\d,]+\\.?\\d{0,2})")

        // Expiry date patterns (YYYY-MM-DD or DD/MM/YYYY or Month DD, YYYY)
        private val EXPIRY_REGEX = Pattern.compile("(?i)(?:exp|valid\\s*thru|valid\\s*till|expiry|expires)[\\s:=-]*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4}|[0-9]{4}-[0-9]{2}-[0-9]{2})")
    }

    /**
     * Extracts structured voucher data strictly matching the requested air-gapped schema.
     */
    fun parseStructuredVoucher(rawInput: String): ExtractedVoucherModel? {
        if (rawInput.isBlank()) return null

        val cleanText = rawInput.replace("\r", " ").trim()

        // 1. Merchant & Subscription detection
        var detectedMerchant: String? = null
        var isSubscription = false

        for (manifest in TemplateRegistry.getAllTemplates()) {
            if (cleanText.contains(manifest.displayName, ignoreCase = true)) {
                detectedMerchant = manifest.displayName
                isSubscription = manifest.category.contains("OTT", ignoreCase = true) || manifest.category.contains("Music", ignoreCase = true)
                break
            }
        }

        if (detectedMerchant == null) {
            val words = cleanText.split(Regex("[\\s,.]+"))
            for (word in words) {
                val t = TemplateRegistry.getTemplateForMerchant(word)
                if (t != null) {
                    detectedMerchant = t.displayName
                    isSubscription = t.category.contains("OTT", ignoreCase = true)
                    break
                }
            }
        }

        // 2. Voucher Code extraction
        var voucherCode: String? = null
        val codeMatcher = CARD_CODE_REGEX.matcher(cleanText)
        if (codeMatcher.find()) {
            voucherCode = codeMatcher.group(1)
        } else {
            val rawMatcher = RAW_CODE_REGEX.matcher(cleanText)
            while (rawMatcher.find()) {
                val candidate = rawMatcher.group(1) ?: continue
                if (!candidate.all { it.isDigit() } || candidate.length >= 14) {
                    voucherCode = candidate
                    break
                }
            }
        }

        // 3. PIN extraction
        var pin: String? = null
        val pinMatcher = PIN_REGEX.matcher(cleanText)
        if (pinMatcher.find()) {
            val candidatePin = pinMatcher.group(1)
            // Ensure PIN is not identical to the card code
            if (candidatePin != voucherCode) {
                pin = candidatePin
            }
        }

        // 4. Face Value extraction
        var faceValue: Double? = null
        val amtMatcher = AMOUNT_REGEX.matcher(cleanText)
        if (amtMatcher.find()) {
            val rawAmt = amtMatcher.group(1)?.replace(",", "")
            val parsed = rawAmt?.toDoubleOrNull()
            if (parsed != null && parsed > 0.0 && parsed <= 500000.0) {
                faceValue = parsed
            }
        }

        // 5. Expiry Date extraction
        var expiryDate: String? = null
        val expMatcher = EXPIRY_REGEX.matcher(cleanText)
        if (expMatcher.find()) {
            expiryDate = expMatcher.group(1)
        }

        if (voucherCode == null && detectedMerchant == null) {
            return null
        }

        return ExtractedVoucherModel(
            merchant = detectedMerchant ?: "Gift Voucher",
            voucher_code = voucherCode,
            pin = pin,
            face_value = faceValue,
            currency = "INR",
            expiry_date = expiryDate,
            is_subscription = isSubscription
        )
    }
}
