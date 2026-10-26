package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.GiftCard
import javax.inject.Inject

class UpdateCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(card: GiftCard) {
        cardRepository.updateCard(card)
    }
}
