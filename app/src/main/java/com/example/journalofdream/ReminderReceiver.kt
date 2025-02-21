package com.example.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.journalofdream.util.REMINDER_HOUR
import com.example.journalofdream.util.REMINDER_MINUTE
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.showNotification

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Будильник сработал!")

        // Проверяем, записал ли пользователь сон
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val todayKey = getTodayKey()
        val dreamRecorded = prefs.getBoolean(todayKey, false)

        if (!dreamRecorded) {
            // Если сон не записан, показываем уведомление
            showNotification(context, "Пора записать сон", "Откройте приложение и запишите ваш сон!")
        }

        // Планируем следующий будильник на фиксированное время (например, 8:00)
        scheduleDailyReminder(context, REMINDER_HOUR, REMINDER_MINUTE)
    }

    /**
     * Формирует ключ для сегодняшней даты.
     */
    private fun getTodayKey(): String {
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = now.get(java.util.Calendar.MONTH) + 1
        val day = now.get(java.util.Calendar.DAY_OF_MONTH)
        return "dream_recorded_${year}_${month}_${day}"
    }
}
