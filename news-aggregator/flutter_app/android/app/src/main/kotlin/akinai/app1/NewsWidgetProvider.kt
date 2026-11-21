package akinai.app1

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetLaunchIntent
import es.antonborri.home_widget.HomeWidgetProvider

/**
 * Ana ekran widget'ı: en çok kaynağın verdiği 3 güncel başlık + güncellenme saati.
 * Veriyi uygulama yazar (lib/services/news_widget.dart → home_widget); dokununca uygulama açılır.
 */
class NewsWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.news_widget).apply {
                setTextViewText(R.id.widget_line1, widgetData.getString("w_line1", null) ?: "Uygulamayı açınca gündem burada görünür")
                setTextViewText(R.id.widget_line2, widgetData.getString("w_line2", null) ?: "")
                setTextViewText(R.id.widget_line3, widgetData.getString("w_line3", null) ?: "")
                setTextViewText(R.id.widget_time, widgetData.getString("w_time", null) ?: "")
                setOnClickPendingIntent(R.id.widget_root, HomeWidgetLaunchIntent.getActivity(context, MainActivity::class.java))
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
