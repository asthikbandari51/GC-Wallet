package com.giftvault.app.domain.usecase

import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.data.repository.TransactionRepository
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.Transaction
import com.giftvault.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UpdateBalanceUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(cardId: String, amount: Double, type: TransactionType, note: String?) {
        val card = cardRepository.getCardById(cardId).first() ?: return
        
        var newBalance = card.balance
        when (type) {
            TransactionType.REDEEM -> newBalance -= amount
            TransactionType.ADD, TransactionType.ADJUSTMENT -> newBalance += amount
        }
        
        if (newBalance < 0) newBalance = 0.0
        
        val newStatus = if (newBalance <= 0) CardStatus.USED else card.status
        
        val updatedCard = card.copy(
            balance = newBalance,
            status = newStatus,
            updatedAt = Instant.now()
        )
        
        cardRepository.updateCard(updatedCard)
        
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            cardId = cardId,
            amount = amount,
            type = type,
            note = note,
            createdAt = Instant.now()
        )
        transactionRepository.addTransaction(transaction)
    }
}
