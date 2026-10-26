package com.giftvault.app.core.suggestions

import com.giftvault.app.domain.model.GiftCard
import kotlinx.coroutines.flow.Flow

interface SuggestionProvider {
    fun generateSuggestions(cards: List<GiftCard>): Flow<List<SmartSuggestion>>
}
