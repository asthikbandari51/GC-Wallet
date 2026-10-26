package com.giftvault.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.giftvault.app.core.database.entity.GiftCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftCardDao {
    @Query("SELECT * FROM gift_cards")
    fun getAllCards(): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_cards WHERE id = :id")
    fun getCardById(id: String): Flow<GiftCardEntity?>

    @Query("SELECT * FROM gift_cards WHERE id = :id")
    suspend fun getCardByIdDirect(id: String): GiftCardEntity?

    @Query("SELECT * FROM gift_cards WHERE merchant_name LIKE '%' || :query || '%'")
    fun searchCards(query: String): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_cards WHERE category = :category")
    fun getCardsByCategory(category: String): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_cards WHERE status = :status")
    fun getCardsByStatus(status: String): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_cards WHERE is_favorite = 1")
    fun getFavoriteCards(): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_cards WHERE expiry_date IS NOT NULL AND expiry_date <= :beforeDate AND status = 'active'")
    fun getExpiringCards(beforeDate: Long): Flow<List<GiftCardEntity>>

    @Query("SELECT SUM(balance) FROM gift_cards WHERE status = 'active' AND category != 'OTT & Streaming'")
    fun getTotalBalance(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: GiftCardEntity)

    @Update
    suspend fun updateCard(card: GiftCardEntity)

    @Delete
    suspend fun deleteCard(card: GiftCardEntity)

    @Query("DELETE FROM gift_cards WHERE id = :id")
    suspend fun deleteCardById(id: String)

    @Query("DELETE FROM gift_cards")
    suspend fun deleteAllCards()
}
