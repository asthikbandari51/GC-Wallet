package com.giftvault.app.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.giftvault.app.core.database.GiftVaultDatabase
import com.giftvault.app.core.database.dao.*
import com.giftvault.app.core.database.entity.GiftCardEntity
import com.giftvault.app.core.database.entity.BankCardEntity
import com.giftvault.app.core.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: GiftVaultDatabase,
    private val cardDao: GiftCardDao,
    private val transactionDao: TransactionDao,
    private val reminderDao: ReminderDao,
    private val receiptDao: ReceiptDao,
    private val bankCardDao: BankCardDao,
    private val truzteeClaimDao: TruzteeClaimDao,
    private val perkQuotaDao: PerkQuotaDao,
    private val cryptoManager: CryptoManager
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private fun reEncryptCardForDevice(card: GiftCardEntity): GiftCardEntity {
        // If the card number can already be decrypted by this device's keystore, it's valid ciphertext.
        // Otherwise, it represents portable plaintext from the backup JSON and must be encrypted
        // with the current device's AndroidKeyStore key for Room storage.
        val decTest = if (card.encryptedCardNumber.isNotBlank()) cryptoManager.decrypt(card.encryptedCardNumber) else ""
        val reEncryptedNum = if (decTest.isNotBlank() || card.encryptedCardNumber.isBlank()) {
            card.encryptedCardNumber
        } else {
            cryptoManager.encrypt(card.encryptedCardNumber)
        }

        val reEncryptedPin = card.encryptedPin?.let { pinStr ->
            val pinDecTest = if (pinStr.isNotBlank()) cryptoManager.decrypt(pinStr) else ""
            if (pinDecTest.isNotBlank() || pinStr.isBlank()) {
                pinStr
            } else {
                cryptoManager.encrypt(pinStr)
            }
        }

        return card.copy(
            encryptedCardNumber = reEncryptedNum,
            encryptedPin = reEncryptedPin
        )
    }

    private fun reEncryptBankCardForDevice(bCard: BankCardEntity): BankCardEntity {
        val numDec = if (bCard.encryptedCardNumber.isNotBlank()) cryptoManager.decrypt(bCard.encryptedCardNumber) else ""
        val encNum = if (numDec.isNotBlank() || bCard.encryptedCardNumber.isBlank()) {
            bCard.encryptedCardNumber
        } else {
            cryptoManager.encrypt(bCard.encryptedCardNumber)
        }

        val cvvDec = if (bCard.encryptedCvv.isNotBlank()) cryptoManager.decrypt(bCard.encryptedCvv) else ""
        val encCvv = if (cvvDec.isNotBlank() || bCard.encryptedCvv.isBlank()) {
            bCard.encryptedCvv
        } else {
            cryptoManager.encrypt(bCard.encryptedCvv)
        }

        val monthDec = if (bCard.encryptedExpiryMonth.isNotBlank()) cryptoManager.decrypt(bCard.encryptedExpiryMonth) else ""
        val encMonth = if (monthDec.isNotBlank() || bCard.encryptedExpiryMonth.isBlank()) {
            bCard.encryptedExpiryMonth
        } else {
            cryptoManager.encrypt(bCard.encryptedExpiryMonth)
        }

        val yearDec = if (bCard.encryptedExpiryYear.isNotBlank()) cryptoManager.decrypt(bCard.encryptedExpiryYear) else ""
        val encYear = if (yearDec.isNotBlank() || bCard.encryptedExpiryYear.isBlank()) {
            bCard.encryptedExpiryYear
        } else {
            cryptoManager.encrypt(bCard.encryptedExpiryYear)
        }

        return bCard.copy(
            encryptedCardNumber = encNum,
            encryptedCvv = encCvv,
            encryptedExpiryMonth = encMonth,
            encryptedExpiryYear = encYear
        )
    }

    suspend fun importBackup(password: String, sourceUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Password cannot be empty"))
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Result.failure(IllegalStateException("Unable to open input stream for Uri: $sourceUri"))

            val salt = ByteArray(16)
            val iv = ByteArray(12)

            val encryptedBytes = inputStream.use { ins ->
                val readSaltSuccess = readExactBytes(ins, salt)
                val readIvSuccess = readExactBytes(ins, iv)
                if (!readSaltSuccess || !readIvSuccess) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid or truncated .giftvault backup file"))
                }
                ins.readBytes()
            }

            if (encryptedBytes.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Backup file contains no encrypted payload"))
            }

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val decryptedBytes = try {
                cipher.doFinal(encryptedBytes)
            } catch (e: Exception) {
                return@withContext Result.failure(IllegalArgumentException("Incorrect password or corrupted backup file"))
            }

            val jsonStr = String(decryptedBytes, StandardCharsets.UTF_8)
            val backupData = try {
                json.decodeFromString<BackupData>(jsonStr)
            } catch (e: Exception) {
                return@withContext Result.failure(IllegalStateException("Failed to parse backup content: ${e.localizedMessage}"))
            }

            val preparedCards = backupData.cards.map { reEncryptCardForDevice(it) }
            val preparedBankCards = backupData.bankCards.map { reEncryptBankCardForDevice(it) }
            val validCardIds = preparedCards.map { it.id }.toSet()

            db.withTransaction {
                receiptDao.deleteAllReceipts()
                transactionDao.deleteAllTransactions()
                reminderDao.deleteAllReminders()
                cardDao.deleteAllCards()
                if (preparedBankCards.isNotEmpty()) {
                    bankCardDao.deleteAll()
                }
                if (backupData.claims.isNotEmpty()) {
                    truzteeClaimDao.deleteAll()
                }
                if (backupData.perkQuotas.isNotEmpty()) {
                    perkQuotaDao.deleteAll()
                }

                preparedCards.forEach { cardDao.insertCard(it) }
                backupData.transactions.filter { it.cardId in validCardIds }.forEach { transactionDao.insertTransaction(it) }
                backupData.reminders.filter { it.cardId in validCardIds }.forEach { reminderDao.insertReminder(it) }
                backupData.receipts.filter { it.cardId in validCardIds }.forEach { receiptDao.insertReceipt(it) }
                preparedBankCards.forEach { bankCardDao.insertBankCard(it) }
                backupData.claims.forEach { truzteeClaimDao.insertClaim(it) }
                if (backupData.perkQuotas.isNotEmpty()) {
                    perkQuotaDao.insertAll(backupData.perkQuotas)
                }
            }

            Result.success(preparedCards.size + preparedBankCards.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromJsonBytes(decryptedBytes: ByteArray): Int = withContext(Dispatchers.IO) {
        val jsonStr = String(decryptedBytes, StandardCharsets.UTF_8)
        val backupData = json.decodeFromString<BackupData>(jsonStr)
        val preparedCards = backupData.cards.map { reEncryptCardForDevice(it) }
        val preparedBankCards = backupData.bankCards.map { reEncryptBankCardForDevice(it) }
        val validCardIds = preparedCards.map { it.id }.toSet()

        db.withTransaction {
            receiptDao.deleteAllReceipts()
            transactionDao.deleteAllTransactions()
            reminderDao.deleteAllReminders()
            cardDao.deleteAllCards()
            if (preparedBankCards.isNotEmpty()) bankCardDao.deleteAll()
            if (backupData.claims.isNotEmpty()) truzteeClaimDao.deleteAll()
            if (backupData.perkQuotas.isNotEmpty()) perkQuotaDao.deleteAll()

            preparedCards.forEach { cardDao.insertCard(it) }
            backupData.transactions.filter { it.cardId in validCardIds }.forEach { transactionDao.insertTransaction(it) }
            backupData.reminders.filter { it.cardId in validCardIds }.forEach { reminderDao.insertReminder(it) }
            backupData.receipts.filter { it.cardId in validCardIds }.forEach { receiptDao.insertReceipt(it) }
            preparedBankCards.forEach { bankCardDao.insertBankCard(it) }
            backupData.claims.forEach { truzteeClaimDao.insertClaim(it) }
            if (backupData.perkQuotas.isNotEmpty()) {
                perkQuotaDao.insertAll(backupData.perkQuotas)
            }
        }
        preparedCards.size + preparedBankCards.size
    }

    private fun readExactBytes(stream: InputStream, target: ByteArray): Boolean {
        var bytesRead = 0
        while (bytesRead < target.size) {
            val read = stream.read(target, bytesRead, target.size - bytesRead)
            if (read == -1) break
            bytesRead += read
        }
        return bytesRead == target.size
    }
}
