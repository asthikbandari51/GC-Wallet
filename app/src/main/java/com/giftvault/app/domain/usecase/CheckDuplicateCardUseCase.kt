package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.GiftCard
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckDuplicateCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cardNumber: String, merchantName: String? = null): GiftCard? {
        if (cardNumber.isBlank()) return null
        val cleanNumber = cardNumber.trim().replace(" ", "").replace("-", "")
        val allCards = cardRepository.getAllCards().first()
        
        return allCards.firstOrNull { existing ->
            val existingClean = existing.cardNumber.trim().replace(" ", "").replace("-", "")
            if (existingClean == cleanNumber) {
                if (merchantName.isNullOrBlank()) {
                    true
                } else {
                    existing.merchantName.equals(merchantName, ignoreCase = true) || existingClean == cleanNumber
                }
            } else {
                false
            }
        }
    }
}
