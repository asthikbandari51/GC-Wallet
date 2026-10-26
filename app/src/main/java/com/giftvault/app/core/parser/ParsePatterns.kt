package com.giftvault.app.core.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

object ParsePatterns {

    // Key labels for standard field identification
    val CODE_KEYS = listOf(
        "coupon code", "coupon no", "coupon number", "voucher code", "voucher no",
        "gift card no", "gift card number", "card no", "card number", "gift code",
        "card code", "code"
    )

    val PIN_KEYS = listOf(
        "pin code", "pin no", "pin number", "pin", "passcode", "security code", "cvv"
    )

    val ISSUE_DATE_KEYS = listOf(
        "date of issue", "issued on", "issue date", "date issued", "purchase date"
    )

    val EXPIRY_DATE_KEYS = listOf(
        "expiry date", "valid till", "expires on", "exp date", "expiry", "exp"
    )

    val MERCHANT_KEYS = listOf(
        "service provider", "merchant name", "merchant", "brand", "store", "provider"
    )

    val SERVICE_KEYS = listOf(
        "service", "category", "service type"
    )

    val DETAILS_KEYS = listOf(
        "service/product details", "product details", "service details", "details", "description"
    )

    val AMOUNT_KEYS = listOf(
        "amount", "value", "denomination", "balance", "price"
    )

    // Regex for combined code and pin on a single line:
    // e.g. "9802004325817067/pin:898443", "Code: 12345678 PIN: 9999", "GYFRR34174757135 - PIN: 1234"
    val COMBINED_CODE_PIN_REGEX = Regex(
        """(?i)([A-Z0-9]{8,24})\s*[\s\/:,\-]+\s*(?:pin|passcode|secret|code)[\s:]*([A-Z0-9]{3,8})""",
        RegexOption.IGNORE_CASE
    )

    // Standalone code pattern (8 to 24 alphanumeric / digits)
    val STANDALONE_CODE_REGEX = Regex("""\b([A-Z0-9]{8,24})\b""", RegexOption.IGNORE_CASE)

    // Standalone PIN pattern (3 to 8 digits / alphanumeric)
    val STANDALONE_PIN_REGEX = Regex("""\b([0-9]{3,8})\b""")

    // Currency / Amount regexes
    val AMOUNT_REGEX = Regex(
        """(?:INR|₹|Rs\.?|Rupees?)\s*:?\s*([\d,]+(?:\.\d{1,2})?)|([\d,]+(?:\.\d{1,2})?)\s*(?:INR|₹|Rs\.?|Rupees?)""",
        RegexOption.IGNORE_CASE
    )

    val NUMERIC_AMOUNT_REGEX = Regex("""\b\d+(?:\.\d{1,2})?\b""")

    // Date formatters
    private val DATE_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MMM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MM/yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MM/yyyy", Locale.ENGLISH)
    )

    val DATE_REGEX = Regex(
        """\b(\d{1,2}[-\/\s](?:[A-Za-z]{3}|\d{1,2})[-\/\s]\d{2,4}|\d{4}[-\/]\d{2}[-\/]\d{2}|\d{1,2}\/\d{2,4})\b"""
    )

    fun parseDate(dateStr: String): LocalDate? {
        val cleanStr = dateStr.trim()
        for (formatter in DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleanStr, formatter)
            } catch (_: Exception) {
            }
        }
        // Handle Month-Year fallback (e.g. 12/26 or 12/2026)
        if (cleanStr.matches(Regex("""\d{1,2}\/\d{2,4}"""))) {
            try {
                val parts = cleanStr.split("/")
                val month = parts[0].toInt()
                var year = parts[1].toInt()
                if (year < 100) year += 2000
                return LocalDate.of(year, month, 1)
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun parseAmount(text: String): Double? {
        val match = AMOUNT_REGEX.find(text)
        if (match != null) {
            val amountStr = (match.groupValues[1].ifEmpty { match.groupValues[2] })
                .replace(",", "")
            return amountStr.toDoubleOrNull()
        }
        return null
    }
}
