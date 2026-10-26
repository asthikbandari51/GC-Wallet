package com.giftvault.app.data.repository

import com.giftvault.app.core.database.dao.TruzteeClaimDao
import com.giftvault.app.data.mappers.toDomain
import com.giftvault.app.data.mappers.toEntity
import com.giftvault.app.domain.model.TruzteeClaim
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TruzteeClaimRepositoryImpl @Inject constructor(
    private val truzteeClaimDao: TruzteeClaimDao
) : TruzteeClaimRepository {

    override fun getAllClaims(): Flow<List<TruzteeClaim>> {
        return truzteeClaimDao.getAllClaims().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveClaims(): Flow<List<TruzteeClaim>> {
        return truzteeClaimDao.getActiveClaims().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getClaimById(id: Long): TruzteeClaim? {
        return truzteeClaimDao.getClaimById(id)?.toDomain()
    }

    override suspend fun insertClaim(claim: TruzteeClaim): Long {
        return truzteeClaimDao.insertClaim(claim.toEntity())
    }

    override suspend fun updateClaim(claim: TruzteeClaim) {
        truzteeClaimDao.updateClaim(claim.toEntity())
    }

    override suspend fun deleteClaim(id: Long) {
        truzteeClaimDao.deleteClaimById(id)
    }
}
