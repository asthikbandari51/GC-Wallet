package com.giftvault.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.giftvault.app.MainActivity
import com.giftvault.app.R

class ExpiringCardsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_expiring).apply {
                setOnClickPendingIntent(R.id.widget_expiring_container, pendingIntent)
                setTextViewText(R.id.widget_expiring_title, "⏰ Expiration Alerts")
                setTextViewText(R.id.widget_expiring_subtitle, "Tap to review upcoming gift card expirations")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
