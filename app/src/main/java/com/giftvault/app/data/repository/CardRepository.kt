package com.giftvault.app.data.repository

import com.giftvault.app.core.database.dao.GiftCardDao
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.core.security.CryptoManager
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.GiftCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface CardRepository {
    fun getAllCards(): Flow<List<GiftCard>>
    fun getCardById(id: String): Flow<GiftCard?>
    fun searchCards(query: String): Flow<List<GiftCard>>
    fun getCardsByCategory(category: CardCategory): Flow<List<GiftCard>>
    fun getFavoriteCards(): Flow<List<GiftCard>>
    fun getExpiringCards(withinDays: Int): Flow<List<GiftCard>>
    fun getTotalBalance(): Flow<Double>
    suspend fun addCard(card: GiftCard)
    suspend fun updateCard(card: GiftCard)
    suspend fun deleteCard(id: String)
    suspend fun archiveCard(id: String)
    suspend fun unarchiveCard(id: String)
}

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val dao: GiftCardDao,
    private val cryptoManager: CryptoManager,
    private val receiptStorageManager: ReceiptStorageManager
) : CardRepository {

    override fun getAllCards(): Flow<List<GiftCard>> = dao.getAllCards().map { entities ->
        entities.map { entity ->
            entity.toDomain(
                decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
                decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
            )
        }
    }.flowOn(Dispatchers.Default)

    override fun getCardById(id: String): Flow<GiftCard?> = dao.getCardById(id).map { entity ->
        entity?.toDomain(
            decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
            decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
        )
    }.flowOn(Dispatchers.Default)

    override fun searchCards(query: String): Flow<List<GiftCard>> = dao.searchCards(query).map { entities ->
        entities.map { entity ->
            entity.toDomain(
                decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
                decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
            )
        }
    }.flowOn(Dispatchers.Default)

    override fun getCardsByCategory(category: CardCategory): Flow<List<GiftCard>> = dao.getCardsByCategory(category.displayName).map { entities ->
        entities.map { entity ->
            entity.toDomain(
                decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
                decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
            )
        }
    }.flowOn(Dispatchers.Default)

    override fun getFavoriteCards(): Flow<List<GiftCard>> = dao.getFavoriteCards().map { entities ->
        entities.map { entity ->
            entity.toDomain(
                decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
                decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
            )
        }
    }.flowOn(Dispatchers.Default)

    override fun getExpiringCards(withinDays: Int): Flow<List<GiftCard>> {
        val targetDate = System.currentTimeMillis() + withinDays * 86400000L
        return dao.getExpiringCards(targetDate).map { entities ->
            entities.map { entity ->
                entity.toDomain(
                    decryptedNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
                    decryptedPin = entity.encryptedPin?.let { cryptoManager.decrypt(it) }
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    override fun getTotalBalance(): Flow<Double> = dao.getTotalBalance().map { it ?: 0.0 }

    override suspend fun addCard(card: GiftCard) {
        val encryptedNumber = cryptoManager.encrypt(card.cardNumber)
        val encryptedPin = card.pin?.let { cryptoManager.encrypt(it) }
        dao.insertCard(card.toEntity(encryptedNumber, encryptedPin))
    }

    override suspend fun updateCard(card: GiftCard) {
        val encryptedNumber = cryptoManager.encrypt(card.cardNumber)
        val encryptedPin = card.pin?.let { cryptoManager.encrypt(it) }
        dao.updateCard(card.toEntity(encryptedNumber, encryptedPin))
    }

    override suspend fun deleteCard(id: String) {
        receiptStorageManager.deleteCardReceiptsDirectory(id)
        dao.deleteCardById(id)
    }

    override suspend fun archiveCard(id: String) {
        val entity = dao.getCardByIdDirect(id) ?: return
        dao.updateCard(entity.copy(status = "archived", updatedAt = System.currentTimeMillis()))
    }

    override suspend fun unarchiveCard(id: String) {
        val entity = dao.getCardByIdDirect(id) ?: return
        dao.updateCard(entity.copy(status = "active", updatedAt = System.currentTimeMillis()))
    }
}
