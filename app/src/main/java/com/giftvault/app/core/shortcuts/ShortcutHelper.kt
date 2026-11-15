package com.giftvault.app.core.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.giftvault.app.MainActivity
import com.giftvault.app.R
import com.giftvault.app.domain.model.GiftCard

object ShortcutHelper {

    const val ACTION_OPEN_CARD = "com.giftvault.ACTION_OPEN_CARD"
    const val EXTRA_CARD_ID = "extra_card_id"

    fun updateDynamicShortcuts(context: Context, cards: List<GiftCard>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        val topCards = cards.filter { it.balance > 0 }
            .sortedWith(compareByDescending<GiftCard> { it.isFavorite }.thenBy { it.expiryDate ?: java.time.LocalDate.MAX })
            .take(3)

        val shortcuts = topCards.mapIndexed { index, card ->
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_CARD
                putExtra(EXTRA_CARD_ID, card.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            ShortcutInfo.Builder(context, "shortcut_card_${card.id}")
                .setShortLabel(card.merchantName)
                .setLongLabel("${card.merchantName} (₹${String.format("%,.0f", card.balance)})")
                .setIcon(Icon.createWithResource(context, R.drawable.logo))
                .setIntent(intent)
                .setRank(index)
                .build()
        }

        try {
            shortcutManager.dynamicShortcuts = shortcuts
        } catch (_: Exception) { }
    }

    fun pinCardToHomeScreen(context: Context, card: GiftCard): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return false
        if (!shortcutManager.isRequestPinShortcutSupported) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_CARD
            putExtra(EXTRA_CARD_ID, card.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pinShortcutInfo = ShortcutInfo.Builder(context, "pinned_card_${card.id}")
            .setShortLabel(card.merchantName)
            .setLongLabel("${card.merchantName} • ₹${String.format("%,.0f", card.balance)}")
            .setIcon(Icon.createWithResource(context, R.drawable.logo))
            .setIntent(intent)
            .build()

        return try {
            shortcutManager.requestPinShortcut(pinShortcutInfo, null)
        } catch (_: Exception) {
            false
        }
    }
}
