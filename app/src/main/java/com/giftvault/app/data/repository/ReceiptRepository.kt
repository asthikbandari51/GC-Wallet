package com.giftvault.app.data.repository

import com.giftvault.app.core.database.dao.ReceiptDao
import com.giftvault.app.core.receipts.ReceiptStorageManager
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

interface ReceiptRepository {
    fun getReceiptsForCard(cardId: String): Flow<List<Receipt>>
    fun getAllReceipts(): Flow<List<Receipt>>
    suspend fun addReceipt(receipt: Receipt)
    suspend fun deleteReceipt(receiptId: String, filePath: String?)
    suspend fun deleteReceiptsForCard(cardId: String)
}

class ReceiptRepositoryImpl @Inject constructor(
    private val dao: ReceiptDao,
    private val storageManager: ReceiptStorageManager
) : ReceiptRepository {

    override fun getReceiptsForCard(cardId: String): Flow<List<Receipt>> {
        return dao.getReceiptsForCard(cardId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return dao.getAllReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addReceipt(receipt: Receipt) {
        dao.insertReceipt(receipt.toEntity())
    }

    override suspend fun deleteReceipt(receiptId: String, filePath: String?) {
        filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        dao.deleteReceiptById(receiptId)
    }

    override suspend fun deleteReceiptsForCard(cardId: String) {
        storageManager.deleteCardReceiptsDirectory(cardId)
        dao.deleteReceiptsForCard(cardId)
    }
}
