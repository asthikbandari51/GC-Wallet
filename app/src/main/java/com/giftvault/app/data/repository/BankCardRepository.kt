package com.giftvault.app.data.repository

import com.giftvault.app.domain.model.BankCard
import kotlinx.coroutines.flow.Flow

interface BankCardRepository {
    fun getAllBankCards(): Flow<List<BankCard>>
    suspend fun getBankCardById(id: Long): BankCard?
    fun getPrimaryBankCard(): Flow<BankCard?>
    suspend fun insertBankCard(card: BankCard): Long
    suspend fun updateBankCard(card: BankCard)
    suspend fun deleteBankCard(cardId: Long)
    suspend fun setPrimaryCard(cardId: Long)
    suspend fun updateQuarterlySpend(cardId: Long, spend: Double)
}
