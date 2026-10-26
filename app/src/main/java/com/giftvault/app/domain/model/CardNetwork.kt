package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CardNetwork(val displayName: String) {
    RUPAY("RuPay"),
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    DINERS("Diners Club");

    companion object {
        fun fromString(value: String?): CardNetwork {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RUPAY
        }
    }
}
