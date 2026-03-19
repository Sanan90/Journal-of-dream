package com.dreamjournal.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dreamjournal.journalofdream.util.scheduleDailyReminder
import com.dreamjournal.journalofdream.util.scheduleQuoteAlarms
import com.dreamjournal.journalofdream.util.scheduleRealityCheck

/**
 * Ресивер, который срабатывает после перезагрузки устройства.
 * Перепланирует ежедневное уведомление, т.к. все AlarmManager будильники
 * сбрасываются при перезагрузке.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val hasHour = prefs.contains("notification_hour")
            val hasMinute = prefs.contains("notification_minute")

            if (hasHour && hasMinute) {
                val hour = prefs.getInt("notification_hour", 8)
                val minute = prefs.getInt("notification_minute", 0)
                scheduleDailyReminder(context, hour, minute)
                Log.d("BootReceiver", "Будильник перепланирован после системного события на $hour:$minute")
            }

            // Перепланируем цитаты если они включены
            val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
            if (quotesEnabled) {
                scheduleQuoteAlarms(context)
                Log.d("BootReceiver", "Цитаты перепланированы после системного события")
            }

            val realityEnabled = prefs.getBoolean("reality_check_enabled", false)
            if (realityEnabled) {
                scheduleRealityCheck(context)
                Log.d("BootReceiver", "Проверка реальности перепланирована после системного события")
            }
        }
    }
}
