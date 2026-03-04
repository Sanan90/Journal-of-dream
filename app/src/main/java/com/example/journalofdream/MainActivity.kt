package com.example.journalofdream

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.ui.theme.AppTheme
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.scheduleQuoteAlarms
import com.example.journalofdream.util.createNotificationChannel
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Создаём канал уведомлений
        createNotificationChannel(this)

        // 2) Запрашиваем разрешение на уведомления (Android 13+)
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

        // 3) Планируем уведомление
        setupNotificationTime()

        // 4) Запускаем приложение — вся логика авторизации внутри JournalOfDreamApp
        setContent {
            AppTheme {
                JournalOfDreamApp()
            }
        }
    }

    /**
     * Если время уведомления ещё не выбрано — показывает TimePickerDialog.
     * Если уже выбрано — просто перепланирует будильник.
     */
    private fun setupNotificationTime() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasHour = prefs.contains("notification_hour")
        val hasMinute = prefs.contains("notification_minute")

        if (!hasHour || !hasMinute) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            android.app.TimePickerDialog(
                this,
                { _, chosenHour, chosenMinute ->
                    prefs.edit()
                        .putInt("notification_hour", chosenHour)
                        .putInt("notification_minute", chosenMinute)
                        .apply()
                    scheduleDailyReminder(this, chosenHour, chosenMinute)
                    Log.d("MainActivity", "Уведомление запланировано на $chosenHour:$chosenMinute")
                },
                hour,
                minute,
                true
            ).apply {
                setTitle("Во сколько напоминать о записи сна?")
                show()
            }
        } else {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Уведомление уже настроено на $hour:$minute")
        }

        // Планируем цитаты если включены
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
        if (quotesEnabled) {
            scheduleQuoteAlarms(this)
        }
    }
}
