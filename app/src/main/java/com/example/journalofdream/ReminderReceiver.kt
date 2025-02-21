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
        Log.d("ReminderReceiver", "onReceive triggered!") // Для отладки

        // Проверяем, записал ли пользователь сон
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val todayKey = getTodayKey()
        val dreamRecorded = prefs.getBoolean(todayKey, false)

        if (!dreamRecorded) {
            // Показываем уведомление
            showNotification(context, "Пора записать сон", "Откройте приложение и запишите ваш сон!")
        }

        // Переназначаем будильник на завтра (текущее время)
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        scheduleDailyReminder(context, hour, minute)
    }

    private fun getTodayKey(): String {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)
        return "dream_recorded_${year}_${month}_${day}"
    }
}
