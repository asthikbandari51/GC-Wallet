package com.giftvault.app.core.database.dao

import androidx.room.*
import com.giftvault.app.core.database.entity.TruzteeClaimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TruzteeClaimDao {

    @Query("SELECT * FROM truztee_claims ORDER BY created_at DESC")
    fun getAllClaims(): Flow<List<TruzteeClaimEntity>>

    @Query("SELECT * FROM truztee_claims WHERE id = :id")
    suspend fun getClaimById(id: Long): TruzteeClaimEntity?

    @Query("SELECT * FROM truztee_claims WHERE status = 'PENDING' OR status = 'DISPUTED' ORDER BY created_at DESC")
    fun getActiveClaims(): Flow<List<TruzteeClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: TruzteeClaimEntity): Long

    @Update
    suspend fun updateClaim(claim: TruzteeClaimEntity)

    @Delete
    suspend fun deleteClaim(claim: TruzteeClaimEntity)

    @Query("DELETE FROM truztee_claims WHERE id = :id")
    suspend fun deleteClaimById(id: Long)

    @Query("DELETE FROM truztee_claims")
    suspend fun deleteAll()
}
