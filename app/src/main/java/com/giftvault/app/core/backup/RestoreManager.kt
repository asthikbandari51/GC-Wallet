package com.giftvault.app.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.giftvault.app.core.database.GiftVaultDatabase
import com.giftvault.app.core.database.dao.*
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
    private val perkQuotaDao: PerkQuotaDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
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

            val validCardIds = backupData.cards.map { it.id }.toSet()

            db.withTransaction {
                receiptDao.deleteAllReceipts()
                transactionDao.deleteAllTransactions()
                reminderDao.deleteAllReminders()
                cardDao.deleteAllCards()
                if (backupData.bankCards.isNotEmpty()) {
                    bankCardDao.deleteAll()
                }
                if (backupData.claims.isNotEmpty()) {
                    truzteeClaimDao.deleteAll()
                }
                if (backupData.perkQuotas.isNotEmpty()) {
                    perkQuotaDao.deleteAll()
                }

                backupData.cards.forEach { cardDao.insertCard(it) }
                backupData.transactions.filter { it.cardId in validCardIds }.forEach { transactionDao.insertTransaction(it) }
                backupData.reminders.filter { it.cardId in validCardIds }.forEach { reminderDao.insertReminder(it) }
                backupData.receipts.filter { it.cardId in validCardIds }.forEach { receiptDao.insertReceipt(it) }
                backupData.bankCards.forEach { bankCardDao.insertBankCard(it) }
                backupData.claims.forEach { truzteeClaimDao.insertClaim(it) }
                if (backupData.perkQuotas.isNotEmpty()) {
                    perkQuotaDao.insertAll(backupData.perkQuotas)
                }
            }

            Result.success(backupData.cards.size + backupData.bankCards.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromJsonBytes(decryptedBytes: ByteArray): Int = withContext(Dispatchers.IO) {
        val jsonStr = String(decryptedBytes, StandardCharsets.UTF_8)
        val backupData = json.decodeFromString<BackupData>(jsonStr)
        val validCardIds = backupData.cards.map { it.id }.toSet()

        db.withTransaction {
            receiptDao.deleteAllReceipts()
            transactionDao.deleteAllTransactions()
            reminderDao.deleteAllReminders()
            cardDao.deleteAllCards()
            if (backupData.bankCards.isNotEmpty()) bankCardDao.deleteAll()
            if (backupData.claims.isNotEmpty()) truzteeClaimDao.deleteAll()
            if (backupData.perkQuotas.isNotEmpty()) perkQuotaDao.deleteAll()

            backupData.cards.forEach { cardDao.insertCard(it) }
            backupData.transactions.filter { it.cardId in validCardIds }.forEach { transactionDao.insertTransaction(it) }
            backupData.reminders.filter { it.cardId in validCardIds }.forEach { reminderDao.insertReminder(it) }
            backupData.receipts.filter { it.cardId in validCardIds }.forEach { receiptDao.insertReceipt(it) }
            backupData.bankCards.forEach { bankCardDao.insertBankCard(it) }
            backupData.claims.forEach { truzteeClaimDao.insertClaim(it) }
            if (backupData.perkQuotas.isNotEmpty()) {
                perkQuotaDao.insertAll(backupData.perkQuotas)
            }
        }
        backupData.cards.size + backupData.bankCards.size
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
