package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CardCategory(val displayName: String) {
    SHOPPING("Shopping"),
    PHARMACY("Pharmacy"),
    GROCERIES("Groceries"),
    ELECTRONICS("Electronics"),
    JEWELLERY("Jewellery"),
    SPA_SALON("Spa & Salon"),
    OTT_STREAMING("OTT & Streaming"),
    FOOD_DINING("Food & Dining"),
    FOOD_DELIVERY("Food Delivery"),
    TRAVEL("Travel"),
    FASHION("Fashion"),
    PAYMENTS("Payments"),
    QUICK_COMMERCE("Quick Commerce"),
    FITNESS("Fitness"),
    ENTERTAINMENT("Entertainment"),
    MUSIC("Music"),
    SPORTS("Sports"),
    OTHERS("Others");

    companion object {
        fun fromDisplayName(name: String): CardCategory {
            val normalized = name.lowercase().replace(Regex("[^a-z0-9]"), "")
            return entries.find {
                it.displayName.lowercase().replace(Regex("[^a-z0-9]"), "") == normalized ||
                it.name.lowercase().replace(Regex("[^a-z0-9]"), "") == normalized
            } ?: OTHERS
        }
    }
}
