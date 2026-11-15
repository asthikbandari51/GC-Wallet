package com.giftvault.app.core.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import com.giftvault.app.domain.model.CardNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NfcReadCardResult(
    val lastFourDigits: String,
    val maskedPan: String,
    val expiryMonth: String,
    val expiryYear: String,
    val network: CardNetwork
)

class NfcCardReader(private val context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun startReaderMode(
        activity: Activity,
        onCardRead: (NfcReadCardResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val adapter = nfcAdapter
        if (adapter == null) {
            onError("NFC is not supported on this device.")
            return
        }
        if (!adapter.isEnabled) {
            onError("NFC is turned off. Please enable NFC in system settings.")
            return
        }

        val flags = NfcAdapter.FLAG_READER_NFC_A or 
                    NfcAdapter.FLAG_READER_NFC_B or 
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        adapter.enableReaderMode(activity, { tag ->
            try {
                val isoDep = IsoDep.get(tag)
                if (isoDep != null) {
                    isoDep.connect()
                    isoDep.timeout = 2000
                    val result = parseEmvCard(isoDep)
                    isoDep.close()
                    if (result != null) {
                        activity.runOnUiThread { onCardRead(result) }
                    } else {
                        activity.runOnUiThread { onError("Could not read public card details. Try holding card firmly to the back of phone.") }
                    }
                } else {
                    activity.runOnUiThread { onError("Unsupported contactless card.") }
                }
            } catch (e: Exception) {
                activity.runOnUiThread { onError("NFC read error: ${e.localizedMessage}") }
            }
        }, flags, options)
    }

    fun stopReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    private fun parseEmvCard(isoDep: IsoDep): NfcReadCardResult? {
        // Standard AIDs to test: RuPay, Visa, Mastercard
        val aids = listOf(
            Pair(byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x05.toByte(), 0x24.toByte(), 0x10.toByte(), 0x10.toByte(), 0x00.toByte()), CardNetwork.RUPAY),
            Pair(byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x03.toByte(), 0x10.toByte(), 0x10.toByte(), 0x00.toByte()), CardNetwork.VISA),
            Pair(byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x10.toByte(), 0x10.toByte(), 0x00.toByte()), CardNetwork.MASTERCARD)
        )

        for ((aidCommand, network) in aids) {
            val response = isoDep.transceive(aidCommand)
            if (response.size >= 2 && response[response.size - 2] == 0x90.toByte()) {
                // Read Record 1 of SFI 1, 2, or 3
                for (sfi in 1..3) {
                    val p2 = ((sfi shl 3) or 4).toByte()
                    val readCommand = byteArrayOf(0x00.toByte(), 0xB2.toByte(), 0x01.toByte(), p2, 0x00.toByte())
                    val recordResponse = isoDep.transceive(readCommand)
                    if (recordResponse.size >= 2 && recordResponse[recordResponse.size - 2] == 0x90.toByte()) {
                        val hex = bytesToHex(recordResponse)
                        val matchResult = extractPanAndExpiryFromHex(hex, network)
                        if (matchResult != null) return matchResult
                    }
                }
            }
        }
        return null
    }

    private fun extractPanAndExpiryFromHex(hex: String, network: CardNetwork): NfcReadCardResult? {
        // Tag 57 or Track 2 data separator 'D'
        val dIdx = hex.indexOf('D')
        if (dIdx >= 16) {
            val panPart = hex.substring(maxOf(0, dIdx - 16), dIdx)
            if (panPart.length >= 15 && panPart.all { it.isDigit() }) {
                val expiryPart = hex.substring(dIdx + 1, minOf(hex.length, dIdx + 5))
                val yr = expiryPart.take(2)
                val mo = expiryPart.takeLast(2)
                val last4 = panPart.takeLast(4)
                val masked = "•••• •••• •••• $last4"
                return NfcReadCardResult(
                    lastFourDigits = last4,
                    maskedPan = masked,
                    expiryMonth = mo,
                    expiryYear = "20$yr",
                    network = network
                )
            }
        }
        return null
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }
}
