package com.giftvault.app.core.receipts

import android.content.Context
import android.net.Uri
import com.giftvault.app.core.database.dao.ReceiptDao
import com.giftvault.app.core.database.entity.ReceiptEntity
import com.giftvault.app.domain.model.Receipt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receiptDao: ReceiptDao,
    private val imageProcessor: ImageProcessor
) {

    suspend fun saveReceiptImage(cardId: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val savedFile = imageProcessor.processAndSaveImage(context, cardId, uri)
        val absolutePath = savedFile.absolutePath

        val receiptId = UUID.randomUUID().toString()
        val entity = ReceiptEntity(
            id = receiptId,
            cardId = cardId,
            filePath = absolutePath,
            createdAt = Instant.now().toEpochMilli()
        )
        receiptDao.insertReceipt(entity)

        absolutePath
    }

    suspend fun deleteReceipt(receiptId: String, filePath: String?) = withContext(Dispatchers.IO) {
        filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        receiptDao.deleteReceiptById(receiptId)
    }

    suspend fun deleteCardReceiptsDirectory(cardId: String) = withContext(Dispatchers.IO) {
        val cardReceiptsDir = File(context.filesDir, "receipts/$cardId")
        if (cardReceiptsDir.exists()) {
            cardReceiptsDir.deleteRecursively()
        }
    }

    fun getReceiptsForCard(cardId: String): Flow<List<Receipt>> {
        return receiptDao.getReceiptsForCard(cardId).map { entities ->
            entities.map {
                Receipt(
                    id = it.id,
                    cardId = it.cardId,
                    filePath = it.filePath,
                    createdAt = Instant.ofEpochMilli(it.createdAt)
                )
            }
        }
    }
}
