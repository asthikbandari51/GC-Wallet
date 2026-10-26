package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class BankCardType(val displayName: String) {
    CREDIT("Credit"),
    DEBIT("Debit");

    companion object {
        fun fromString(value: String?): BankCardType {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
            } ?: CREDIT
        }
    }
}

@Immutable
data class BankCard(
    val id: Long = 0L,
    val cardNickname: String,
    val bankName: String,
    val cardType: BankCardType = BankCardType.CREDIT,
    val network: CardNetwork,
    val tier: CardTier,
    val cardNumber: String,
    val cvv: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cardholderName: String = "",
    val isPrimary: Boolean = false,
    val quarterlySpendTarget: Double = 0.0, // 0.0 = no spend target
    val currentQuarterSpend: Double = 0.0
) {
    val lastFourDigits: String
        get() = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "••••"

    val maskedDisplayNumber: String
        get() = "•••• •••• •••• $lastFourDigits"

    val formattedExpiry: String
        get() = if (expiryMonth.isNotBlank() && expiryYear.isNotBlank()) {
            val yr = if (expiryYear.length == 4) expiryYear.takeLast(2) else expiryYear
            "$expiryMonth/$yr"
        } else {
            "••/••"
        }

    val hasSpendTarget: Boolean
        get() = quarterlySpendTarget > 0.0

    val quarterlyProgressFraction: Float
        get() = if (quarterlySpendTarget > 0.0) {
            (currentQuarterSpend / quarterlySpendTarget).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val quarterlyPercentage: Int
        get() = (quarterlyProgressFraction * 100).toInt()
}
