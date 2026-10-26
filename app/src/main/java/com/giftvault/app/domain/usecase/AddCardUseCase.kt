package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.GiftCard
import javax.inject.Inject

class AddCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(card: GiftCard) {
        cardRepository.addCard(card)
    }
}
