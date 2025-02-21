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

        // Проверяем, записал ли пользователь сон
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val todayKey = getTodayKey()
        val dreamRecorded = prefs.getBoolean(todayKey, false)

        if (!dreamRecorded) {
            showNotification(context, "Пора записать сон", "Откройте приложение и запишите ваш сон!")
        }

        // Считываем время уведомления из настроек, если оно задано, иначе используем 8:00
        val hour = prefs.getInt("notification_hour", 8)
        val minute = prefs.getInt("notification_minute", 0)
        scheduleDailyReminder(context, hour, minute)
        Log.d("ReminderReceiver", "Следующий будильник запланирован на $hour:$minute")
    }

    private fun getTodayKey(): String {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)
        return "dream_recorded_${year}_${month}_${day}"
    }
}
