package com.giftvault.app.data.repository

import com.giftvault.app.domain.model.TruzteeClaim
import kotlinx.coroutines.flow.Flow

interface TruzteeClaimRepository {
    fun getAllClaims(): Flow<List<TruzteeClaim>>
    fun getActiveClaims(): Flow<List<TruzteeClaim>>
    suspend fun getClaimById(id: Long): TruzteeClaim?
    suspend fun insertClaim(claim: TruzteeClaim): Long
    suspend fun updateClaim(claim: TruzteeClaim)
    suspend fun deleteClaim(id: Long)
}
