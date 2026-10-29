package com.giftvault.app.core.backup

import android.content.Context
import android.net.Uri
import com.giftvault.app.core.database.dao.*
import com.giftvault.app.core.database.entity.*
import com.giftvault.app.core.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = 0L,
    val cards: List<GiftCardEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val receipts: List<ReceiptEntity> = emptyList(),
    val bankCards: List<BankCardEntity> = emptyList(),
    val claims: List<TruzteeClaimEntity> = emptyList(),
    val perkQuotas: List<PerkQuotaEntity> = emptyList()
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
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
        encodeDefaults = true
        coerceInputValues = true
    }
    private val currentVersion = 5

    private suspend fun buildPortableBackupData(): BackupData {
        val rawCards = cardDao.getAllCards().first()
        val rawBankCards = bankCardDao.getAllBankCards().first()
        val transactions = transactionDao.getAllTransactions().first()
        val reminders = reminderDao.getAllReminders().first()
        val receipts = receiptDao.getAllReceipts().first()
        val claims = truzteeClaimDao.getAllClaims().first()
        val perkQuotas = perkQuotaDao.getAllQuotas().first()

        // Decrypt sensitive fields using local cryptoManager so the backup JSON stores
        // device-independent plaintext protected exclusively by the backup file's password
        val portableCards = rawCards.map { card ->
            val decNum = cryptoManager.decrypt(card.encryptedCardNumber)
            val plainNum = if (decNum.isNotBlank()) decNum else card.encryptedCardNumber

            val decPin = card.encryptedPin?.let { pinStr ->
                val p = cryptoManager.decrypt(pinStr)
                if (p.isNotBlank()) p else pinStr
            }

            card.copy(
                encryptedCardNumber = plainNum,
                encryptedPin = decPin
            )
        }

        val portableBankCards = rawBankCards.map { bCard ->
            val decNum = cryptoManager.decrypt(bCard.encryptedCardNumber)
            val plainNum = if (decNum.isNotBlank()) decNum else bCard.encryptedCardNumber

            val decCvv = cryptoManager.decrypt(bCard.encryptedCvv)
            val plainCvv = if (decCvv.isNotBlank()) decCvv else bCard.encryptedCvv

            val decMonth = cryptoManager.decrypt(bCard.encryptedExpiryMonth)
            val plainMonth = if (decMonth.isNotBlank()) decMonth else bCard.encryptedExpiryMonth

            val decYear = cryptoManager.decrypt(bCard.encryptedExpiryYear)
            val plainYear = if (decYear.isNotBlank()) decYear else bCard.encryptedExpiryYear

            bCard.copy(
                encryptedCardNumber = plainNum,
                encryptedCvv = plainCvv,
                encryptedExpiryMonth = plainMonth,
                encryptedExpiryYear = plainYear
            )
        }

        return BackupData(
            version = currentVersion,
            timestamp = System.currentTimeMillis(),
            cards = portableCards,
            transactions = transactions,
            reminders = reminders,
            receipts = receipts,
            bankCards = portableBankCards,
            claims = claims,
            perkQuotas = perkQuotas
        )
    }

    suspend fun createBackupDataJsonBytes(): ByteArray = withContext(Dispatchers.IO) {
        val backupData = buildPortableBackupData()
        json.encodeToString(backupData).toByteArray(StandardCharsets.UTF_8)
    }

    suspend fun exportBackup(password: String, destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = buildPortableBackupData()
            val jsonStr = json.encodeToString(backupData)
            val jsonBytes = jsonStr.toByteArray(StandardCharsets.UTF_8)

            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val encryptedData = cipher.doFinal(jsonBytes)

            val outputStream = context.contentResolver.openOutputStream(destinationUri)
                ?: return@withContext Result.failure(Exception("Unable to open output stream"))

            outputStream.use { os ->
                os.write(salt)
                os.write(iv)
                os.write(encryptedData)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
