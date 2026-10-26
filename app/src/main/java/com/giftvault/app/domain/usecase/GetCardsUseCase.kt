package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.GiftCard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(): Flow<List<GiftCard>> = cardRepository.getAllCards()
    fun favorites(): Flow<List<GiftCard>> = cardRepository.getFavoriteCards()
    fun byCategory(category: CardCategory): Flow<List<GiftCard>> = cardRepository.getCardsByCategory(category)
    fun expiringSoon(days: Int = 30): Flow<List<GiftCard>> = cardRepository.getExpiringCards(days)
    fun search(query: String): Flow<List<GiftCard>> = cardRepository.searchCards(query)
}
