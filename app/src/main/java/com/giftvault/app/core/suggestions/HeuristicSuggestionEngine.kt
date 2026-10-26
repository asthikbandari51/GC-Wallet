package com.giftvault.app.core.suggestions

import com.giftvault.app.domain.model.GiftCard
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class HeuristicSuggestionEngine @Inject constructor() : SuggestionProvider {

    override fun generateSuggestions(cards: List<GiftCard>): Flow<List<SmartSuggestion>> = flow {
        val suggestions = mutableListOf<SmartSuggestion>()
        val today = LocalDate.now()
        val nowMillis = System.currentTimeMillis()
        val dayInMillis = 24L * 60 * 60 * 1000

        cards.forEach { card ->
            // Rule 1: Expiring within 14 days and balance > 0
            val expiry = card.expiryDate
            if (expiry != null && card.balance > 0) {
                val daysToExpiry = ChronoUnit.DAYS.between(today, expiry)
                if (daysToExpiry in 0..14) {
                    suggestions.add(
                        SmartSuggestion(
                            id = UUID.randomUUID().toString(),
                            title = "Use Soonest Expiring",
                            description = "Card expires in $daysToExpiry days",
                            card = card,
                            priority = SuggestionPriority.HIGH,
                            type = SuggestionType.EXPIRING_SOON
                        )
                    )
                }
            }

            // Rule 2: High Balance Unused (balance > 2000, not modified in 30 days)
            if (card.balance > 2000.0) {
                val lastModified = card.updatedAt.toEpochMilli()
                val daysSinceModified = (nowMillis - lastModified) / dayInMillis
                if (daysSinceModified >= 30) {
                    suggestions.add(
                        SmartSuggestion(
                            id = UUID.randomUUID().toString(),
                            title = "High Unused Balance",
                            description = "You have a large balance sitting idle for over 30 days",
                            card = card,
                            priority = SuggestionPriority.MEDIUM,
                            type = SuggestionType.HIGH_BALANCE_UNUSED
                        )
                    )
                }
            }
        }

        // Rule 3: Best Value (Card with highest remaining balance)
        val highestBalanceCard = cards.maxByOrNull { it.balance }
        if (highestBalanceCard != null && highestBalanceCard.balance > 0) {
            suggestions.add(
                SmartSuggestion(
                    id = UUID.randomUUID().toString(),
                    title = "Top Remaining Value",
                    description = "This card has your highest remaining balance",
                    card = highestBalanceCard,
                    priority = SuggestionPriority.LOW,
                    type = SuggestionType.BEST_VALUE
                )
            )
        }

        val sortedSuggestions = suggestions.sortedBy { it.priority }
        emit(sortedSuggestions)
    }
}
