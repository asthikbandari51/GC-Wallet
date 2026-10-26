package com.giftvault.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {
    companion object {
        private const val KEYSTORE_ALIAS = "giftvault_master_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 128
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    @Volatile
    private var cachedSecretKey: SecretKey? = null

    // High-performance thread-safe in-memory cache for decrypted card/pin strings
    private val decryptionCache = ConcurrentHashMap<String, String>()

    private fun getOrCreateKey(): SecretKey {
        return cachedSecretKey ?: synchronized(this) {
            cachedSecretKey ?: run {
                val key = if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                    (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
                } else {
                    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()

                    keyGenerator.init(keyGenParameterSpec)
                    keyGenerator.generateKey()
                }
                cachedSecretKey = key
                key
            }
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext
        val encoded = Base64.encodeToString(combined, Base64.DEFAULT)
        decryptionCache[encoded] = plainText
        return encoded
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        decryptionCache[encryptedText]?.let { return it }

        try {
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            val iv = combined.copyOfRange(0, IV_LENGTH)
            val ciphertext = combined.copyOfRange(IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            val plainText = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
            decryptionCache[encryptedText] = plainText
            return plainText
        } catch (e: Exception) {
            return ""
        }
    }
}
