package com.example.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.showNotification
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Будильник сработал!")

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled) return

        // Проверяем записал ли пользователь сон сегодня
        val todayKey = getTodayKey()
        val dreamRecorded = prefs.getBoolean(todayKey, false)

        if (!dreamRecorded) {
            showNotification(
                context,
                title = "Пора записать сон 🌙",
                message = "Запишите сон пока воспоминания свежие!"
            )
        }

        // Планируем следующее напоминание
        val hour = prefs.getInt("notification_hour", 8)
        val minute = prefs.getInt("notification_minute", 0)
        scheduleDailyReminder(context, hour, minute)
        Log.d("ReminderReceiver", "Следующий будильник на $hour:$minute")
    }

    private fun getTodayKey(): String {
        val now = Calendar.getInstance()
        return "dream_recorded_${now.get(Calendar.YEAR)}_${now.get(Calendar.MONTH) + 1}_${now.get(Calendar.DAY_OF_MONTH)}"
    }
}
