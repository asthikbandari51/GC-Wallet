package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.TruzteeClaimEntity
import com.giftvault.app.domain.model.ClaimStatus
import com.giftvault.app.domain.model.TruzteeClaim
import java.time.Instant

fun TruzteeClaimEntity.toDomain(): TruzteeClaim {
    return TruzteeClaim(
        id = this.id,
        orderId = this.orderId,
        transactionId = this.transactionId,
        merchantName = this.merchantName,
        voucherAmount = this.voucherAmount,
        status = ClaimStatus.fromString(this.status),
        bankCardId = this.bankCardId,
        bankCardNickname = this.bankCardNickname,
        notes = this.notes,
        createdAt = Instant.ofEpochMilli(this.createdAt),
        resolvedAt = this.resolvedAt?.let { Instant.ofEpochMilli(it) }
    )
}

fun TruzteeClaim.toEntity(): TruzteeClaimEntity {
    return TruzteeClaimEntity(
        id = this.id,
        orderId = this.orderId,
        transactionId = this.transactionId,
        merchantName = this.merchantName,
        voucherAmount = this.voucherAmount,
        status = this.status.name,
        bankCardId = this.bankCardId,
        bankCardNickname = this.bankCardNickname,
        notes = this.notes,
        createdAt = this.createdAt.toEpochMilli(),
        resolvedAt = this.resolvedAt?.toEpochMilli()
    )
}
