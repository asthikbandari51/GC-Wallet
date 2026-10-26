package com.giftvault.app.core.suggestions

import com.giftvault.app.domain.model.GiftCard

enum class SuggestionPriority {
    HIGH, MEDIUM, LOW
}

enum class SuggestionType {
    EXPIRING_SOON, HIGH_BALANCE_UNUSED, RECENTLY_IDLE, BEST_VALUE
}

data class SmartSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val card: GiftCard,
    val priority: SuggestionPriority,
    val type: SuggestionType
)
