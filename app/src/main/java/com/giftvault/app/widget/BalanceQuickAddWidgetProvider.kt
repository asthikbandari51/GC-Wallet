package com.giftvault.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.giftvault.app.MainActivity
import com.giftvault.app.R

class BalanceQuickAddWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("EXTRA_NAVIGATE_ADD", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_balance_quickadd).apply {
                setOnClickPendingIntent(R.id.widget_balance_container, pendingIntent)
                setTextViewText(R.id.widget_balance_title, "Vault Balance & Quick Add")
                setTextViewText(R.id.widget_balance_subtitle, "1-Tap Add Gift Card")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
