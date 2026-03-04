package com.example.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.scheduleQuoteAlarms

/**
 * Ресивер, который срабатывает после перезагрузки устройства.
 * Перепланирует ежедневное уведомление, т.к. все AlarmManager будильники
 * сбрасываются при перезагрузке.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val hasHour = prefs.contains("notification_hour")
            val hasMinute = prefs.contains("notification_minute")

            if (hasHour && hasMinute) {
                val hour = prefs.getInt("notification_hour", 8)
                val minute = prefs.getInt("notification_minute", 0)
                scheduleDailyReminder(context, hour, minute)
                Log.d("BootReceiver", "Будильник перепланирован после перезагрузки на $hour:$minute")
            }

            // Перепланируем цитаты если они включены
            val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
            if (quotesEnabled) {
                scheduleQuoteAlarms(context)
                Log.d("BootReceiver", "Цитаты перепланированы после перезагрузки")
            }
        }
    }
}
