package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.BankCardEntity
import com.giftvault.app.core.security.CryptoManager
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.BankCardType
import com.giftvault.app.domain.model.CardNetwork
import com.giftvault.app.domain.model.CardTier

fun BankCardEntity.toDomain(cryptoManager: CryptoManager): BankCard {
    val net = CardNetwork.fromString(this.network)
    val cType = BankCardType.fromString(this.cardType)
    val decryptedCardNum = if (this.encryptedCardNumber.isNotBlank()) {
        try { cryptoManager.decrypt(this.encryptedCardNumber) } catch (_: Exception) { "" }
    } else ""
    val decryptedCvv = if (this.encryptedCvv.isNotBlank()) {
        try { cryptoManager.decrypt(this.encryptedCvv) } catch (_: Exception) { "" }
    } else ""
    val decryptedMonth = if (this.encryptedExpiryMonth.isNotBlank()) {
        try { cryptoManager.decrypt(this.encryptedExpiryMonth) } catch (_: Exception) { "" }
    } else ""
    val decryptedYear = if (this.encryptedExpiryYear.isNotBlank()) {
        try { cryptoManager.decrypt(this.encryptedExpiryYear) } catch (_: Exception) { "" }
    } else ""

    return BankCard(
        id = this.id,
        cardNickname = this.cardNickname,
        bankName = this.bankName,
        cardType = cType,
        network = net,
        tier = CardTier.fromString(this.tier, net),
        cardNumber = decryptedCardNum,
        cvv = decryptedCvv,
        expiryMonth = decryptedMonth,
        expiryYear = decryptedYear,
        cardholderName = this.cardholderName,
        isPrimary = this.isPrimary,
        quarterlySpendTarget = this.quarterlySpendTarget,
        currentQuarterSpend = this.currentQuarterSpend
    )
}

fun BankCard.toEntity(cryptoManager: CryptoManager): BankCardEntity {
    val encCardNum = if (this.cardNumber.isNotBlank()) cryptoManager.encrypt(this.cardNumber) else ""
    val encCvv = if (this.cvv.isNotBlank()) cryptoManager.encrypt(this.cvv) else ""
    val encMonth = if (this.expiryMonth.isNotBlank()) cryptoManager.encrypt(this.expiryMonth) else ""
    val encYear = if (this.expiryYear.isNotBlank()) cryptoManager.encrypt(this.expiryYear) else ""

    return BankCardEntity(
        id = this.id,
        cardNickname = this.cardNickname,
        bankName = this.bankName,
        cardType = this.cardType.name,
        network = this.network.name,
        tier = this.tier.name,
        encryptedCardNumber = encCardNum,
        encryptedCvv = encCvv,
        encryptedExpiryMonth = encMonth,
        encryptedExpiryYear = encYear,
        lastFourDigits = this.lastFourDigits,
        cardholderName = this.cardholderName,
        isPrimary = this.isPrimary,
        quarterlySpendTarget = this.quarterlySpendTarget,
        currentQuarterSpend = this.currentQuarterSpend
    )
}
