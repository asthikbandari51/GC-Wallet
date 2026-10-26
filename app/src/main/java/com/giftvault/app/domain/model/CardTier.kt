package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CardTier(val displayName: String, val network: CardNetwork? = null) {
    // RuPay variants
    RUPAY_CLASSIC("Classic", CardNetwork.RUPAY),
    RUPAY_PLATINUM("Platinum", CardNetwork.RUPAY),
    RUPAY_SELECT("Select", CardNetwork.RUPAY),
    RUPAY_EKAA("EKAA", CardNetwork.RUPAY),

    // Visa variants
    VISA_CLASSIC("Classic", CardNetwork.VISA),
    VISA_GOLD("Gold", CardNetwork.VISA),
    VISA_PLATINUM("Platinum", CardNetwork.VISA),
    VISA_SIGNATURE("Signature", CardNetwork.VISA),
    VISA_INFINITE("Infinite", CardNetwork.VISA),

    // Mastercard variants
    MC_CLASSIC("Classic", CardNetwork.MASTERCARD),
    MC_PLATINUM_TITANIUM("Platinum / Titanium", CardNetwork.MASTERCARD),
    MC_WORLD("World", CardNetwork.MASTERCARD),
    MC_WORLD_ELITE("World Elite", CardNetwork.MASTERCARD),

    // Generic / Other
    GENERIC_CLASSIC("Classic"),
    GENERIC_PLATINUM("Platinum"),
    GENERIC_SELECT("Select"),
    GENERIC_REWARD("Reward");

    companion object {
        fun getTiersForNetwork(network: CardNetwork): List<CardTier> {
            return when (network) {
                CardNetwork.RUPAY -> listOf(RUPAY_CLASSIC, RUPAY_PLATINUM, RUPAY_SELECT, RUPAY_EKAA)
                CardNetwork.VISA -> listOf(VISA_CLASSIC, VISA_GOLD, VISA_PLATINUM, VISA_SIGNATURE, VISA_INFINITE)
                CardNetwork.MASTERCARD -> listOf(MC_CLASSIC, MC_PLATINUM_TITANIUM, MC_WORLD, MC_WORLD_ELITE)
                CardNetwork.AMEX -> listOf(GENERIC_CLASSIC, GENERIC_PLATINUM, GENERIC_SELECT, GENERIC_REWARD)
                CardNetwork.DINERS -> listOf(GENERIC_CLASSIC, GENERIC_PLATINUM, GENERIC_SELECT, GENERIC_REWARD)
            }
        }

        fun fromString(value: String?, network: CardNetwork = CardNetwork.RUPAY): CardTier {
            val tier = entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
            if (tier != null) return tier
            return getTiersForNetwork(network).firstOrNull() ?: RUPAY_SELECT
        }
    }
}
