package com.giftvault.app.core.database.dao

import androidx.room.*
import com.giftvault.app.core.database.entity.PerkQuotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerkQuotaDao {

    @Query("SELECT * FROM perk_quotas WHERE card_id = :cardId ORDER BY perk_type ASC")
    fun getQuotasForCard(cardId: Long): Flow<List<PerkQuotaEntity>>

    @Query("SELECT * FROM perk_quotas ORDER BY card_id ASC, perk_type ASC")
    fun getAllQuotas(): Flow<List<PerkQuotaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuota(quota: PerkQuotaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotas: List<PerkQuotaEntity>)

    @Update
    suspend fun updateQuota(quota: PerkQuotaEntity)

    @Delete
    suspend fun deleteQuota(quota: PerkQuotaEntity)

    @Query("UPDATE perk_quotas SET used_count = used_count + 1 WHERE id = :id AND used_count < total_entitlement")
    suspend fun incrementUsed(id: Long): Int

    @Query("UPDATE perk_quotas SET used_count = 0, period_start_epoch = :newStart, period_end_epoch = :newEnd WHERE id = :id")
    suspend fun resetPeriod(id: Long, newStart: Long, newEnd: Long)

    @Query("DELETE FROM perk_quotas")
    suspend fun deleteAll()
}
