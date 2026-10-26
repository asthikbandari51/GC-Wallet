package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import javax.inject.Inject

class DeleteCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cardId: String) {
        cardRepository.deleteCard(cardId)
    }
}
