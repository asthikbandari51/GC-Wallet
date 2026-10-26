package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cardId: String) {
        val card = cardRepository.getCardById(cardId).first() ?: return
        val updatedCard = card.copy(
            isFavorite = !card.isFavorite,
            updatedAt = Instant.now()
        )
        cardRepository.updateCard(updatedCard)
    }
}
