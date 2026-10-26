package com.giftvault.app.core.database.dao

import androidx.room.*
import com.giftvault.app.core.database.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankCardDao {

    @Query("SELECT * FROM bank_cards ORDER BY is_primary DESC, id ASC")
    fun getAllBankCards(): Flow<List<BankCardEntity>>

    @Query("SELECT * FROM bank_cards WHERE id = :id")
    suspend fun getBankCardById(id: Long): BankCardEntity?

    @Query("SELECT * FROM bank_cards WHERE is_primary = 1 LIMIT 1")
    fun getPrimaryBankCard(): Flow<BankCardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankCard(card: BankCardEntity): Long

    @Update
    suspend fun updateBankCard(card: BankCardEntity)

    @Delete
    suspend fun deleteBankCard(card: BankCardEntity)

    @Query("DELETE FROM bank_cards WHERE id = :id")
    suspend fun deleteBankCardById(id: Long)

    @Query("UPDATE bank_cards SET is_primary = 0 WHERE id != :primaryCardId")
    suspend fun clearOtherPrimaries(primaryCardId: Long)

    @Query("UPDATE bank_cards SET current_quarter_spend = :spend WHERE id = :cardId")
    suspend fun updateQuarterlySpend(cardId: Long, spend: Double)

    @Query("DELETE FROM bank_cards")
    suspend fun deleteAll()
}
