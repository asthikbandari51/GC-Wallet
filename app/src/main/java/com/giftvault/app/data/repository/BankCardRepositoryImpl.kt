package com.giftvault.app.data.repository

import com.giftvault.app.core.database.dao.BankCardDao
import com.giftvault.app.core.security.CryptoManager
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.BankCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankCardRepositoryImpl @Inject constructor(
    private val bankCardDao: BankCardDao,
    private val cryptoManager: CryptoManager
) : BankCardRepository {

    override fun getAllBankCards(): Flow<List<BankCard>> {
        return bankCardDao.getAllBankCards().map { entities ->
            entities.map { it.toDomain(cryptoManager) }
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun getBankCardById(id: Long): BankCard? {
        return bankCardDao.getBankCardById(id)?.toDomain(cryptoManager)
    }

    override fun getPrimaryBankCard(): Flow<BankCard?> {
        return bankCardDao.getPrimaryBankCard().map { it?.toDomain(cryptoManager) }.flowOn(Dispatchers.Default)
    }

    override suspend fun insertBankCard(card: BankCard): Long {
        val id = bankCardDao.insertBankCard(card.toEntity(cryptoManager))
        if (card.isPrimary) {
            bankCardDao.clearOtherPrimaries(id)
        }
        return id
    }

    override suspend fun updateBankCard(card: BankCard) {
        bankCardDao.updateBankCard(card.toEntity(cryptoManager))
        if (card.isPrimary) {
            bankCardDao.clearOtherPrimaries(card.id)
        }
    }

    override suspend fun deleteBankCard(cardId: Long) {
        bankCardDao.deleteBankCardById(cardId)
    }

    override suspend fun setPrimaryCard(cardId: Long) {
        val card = bankCardDao.getBankCardById(cardId)
        if (card != null) {
            bankCardDao.updateBankCard(card.copy(isPrimary = true))
            bankCardDao.clearOtherPrimaries(cardId)
        }
    }

    override suspend fun updateQuarterlySpend(cardId: Long, spend: Double) {
        bankCardDao.updateQuarterlySpend(cardId, spend)
    }
}
