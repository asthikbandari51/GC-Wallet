package com.giftvault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.giftvault.app.core.database.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts WHERE card_id = :cardId ORDER BY created_at DESC")
    fun getReceiptsForCard(cardId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts ORDER BY created_at DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: String)

    @Query("DELETE FROM receipts WHERE card_id = :cardId")
    suspend fun deleteReceiptsForCard(cardId: String)

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()
}
