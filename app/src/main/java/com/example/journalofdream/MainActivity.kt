package com.example.journalofdream

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.ui.theme.AppTheme
import com.example.journalofdream.util.LocaleHelper
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.scheduleQuoteAlarms
import com.example.journalofdream.util.createNotificationChannel
import java.util.Calendar

class MainActivity : ComponentActivity() {

    // Применяем язык до того как Activity создаёт View
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Создаём канал уведомлений
        createNotificationChannel(this)

        // 2) Запрашиваем разрешение POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }

        // 3) Проверяем разрешение на точные алармы (Android 12+)
        // Без него уведомления могут приходить с большой задержкой или не приходить
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Нет разрешения на точные алармы — открываем настройки")
                // Открываем системные настройки для выдачи разрешения
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        // 4) Планируем уведомления
        setupNotificationTime()

        // 5) Запускаем приложение
        setContent {
            AppTheme {
                JournalOfDreamApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Перепланируем алармы после возврата из настроек ТОЛЬКО если время уже было выбрано ранее
        // (чтобы не показывать TimePickerDialog повторно)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                if (prefs.contains("notification_hour") && prefs.contains("notification_minute")) {
                    val hour = prefs.getInt("notification_hour", 8)
                    val minute = prefs.getInt("notification_minute", 0)
                    scheduleDailyReminder(this, hour, minute)
                    val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
                    if (quotesEnabled) scheduleQuoteAlarms(this)
                }
            }
        }
    }

    /**
     * Перепланирует будильники если время уже было выбрано пользователем.
     * Первичная настройка происходит в NotificationSetupScreen (внутри приложения).
     */
    private fun setupNotificationTime() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Перепланируем только если время уже выбрано (первичная настройка — в NotificationSetupScreen)
        if (prefs.contains("notification_hour") && prefs.contains("notification_minute")) {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Уведомление перепланировано на $hour:$minute")
        }

        // Планируем цитаты если включены
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
        if (quotesEnabled) {
            scheduleQuoteAlarms(this)
        }
    }
}
