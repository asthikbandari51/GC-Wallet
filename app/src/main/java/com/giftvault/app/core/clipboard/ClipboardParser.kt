package com.giftvault.app.core.clipboard

import javax.inject.Inject
import javax.inject.Singleton

data class ParsedCardData(
    val merchantName: String? = null,
    val cardNumber: String? = null,
    val pin: String? = null,
    val balance: Double? = null,
    val expiryDateStr: String? = null
)

@Singleton
class ClipboardParser @Inject constructor() {

    fun parseText(text: String): ParsedCardData {
        val cardNumberRegex = Regex("\\b(?:\\d[ -]*?){12,19}\\b")
        val pinRegex = Regex("(?i)(?:PIN|CVV)[\\s:]*([0-9]{4,6})\\b")
        val balanceRegex = Regex("(?i)(?:₹|INR|Rs\\.?|Balance:?)\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
        
        val knownMerchants = listOf("Reliance", "Amazon", "Flipkart", "Myntra", "Swiggy", "Zomato", "Uber", "Ola", "Blinkit", "Zepto")
        
        var merchantName: String? = null
        for (merchant in knownMerchants) {
            if (text.contains(merchant, ignoreCase = true)) {
                merchantName = merchant
                break
            }
        }
        
        val cardNumber = cardNumberRegex.find(text)?.value?.replace(Regex("[ -]"), "")
        val pin = pinRegex.find(text)?.groups?.get(1)?.value
        val balanceStr = balanceRegex.find(text)?.groups?.get(1)?.value
        val balance = balanceStr?.toDoubleOrNull()
        
        // Basic date extraction heuristic MM/YY or DD/MM/YYYY
        val expiryRegex = Regex("\\b(?:\\d{2}/\\d{2}|\\d{2}/\\d{2}/\\d{4})\\b")
        val expiryDateStr = expiryRegex.find(text)?.value
        
        return ParsedCardData(
            merchantName = merchantName,
            cardNumber = cardNumber,
            pin = pin,
            balance = balance,
            expiryDateStr = expiryDateStr
        )
    }
}
