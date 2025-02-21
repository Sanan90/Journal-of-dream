package com.example.journalofdream

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.createNotificationChannel
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём канал уведомлений
        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPostNotificationPermission()
            } else {
                handleNotificationTime()
            }
        } else {
            handleNotificationTime()
        }

        setContent {
            JournalOfDreamApp()
        }
    }

    /**
     * Если время для уведомлений не задано, показываем TimePickerDialog,
     * иначе планируем уведомление согласно сохранённому времени.
     */
    private fun handleNotificationTime() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("notification_hour") || !prefs.contains("notification_minute")) {
            // Время не выбрано – предлагаем пользователю выбрать
            showTimePickerDialog()
        } else {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Уведомление запланировано на $hour:$minute")
        }
    }

    /**
     * Показывает диалог выбора времени с пояснением и сохраняет выбранное время.
     */
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hourOfDay, minute ->
            // Сохраняем выбранное время
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            prefs.putInt("notification_hour", hourOfDay)
            prefs.putInt("notification_minute", minute)
            prefs.apply()

            scheduleDailyReminder(this, hourOfDay, minute)
            Log.d("MainActivity", "Пользователь выбрал время: $hourOfDay:$minute")
        }, currentHour, currentMinute, true).apply {
            setTitle("В какое время напомнить о записи сна?\nВыберите время, когда вы обычно просыпаетесь, пока сон еще свеж в памяти.")
            show()
        }
    }

    /**
     * Запрашивает разрешение POST_NOTIFICATIONS для Android 13+.
     */
    private fun requestPostNotificationPermission() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "Разрешение получено")
                handleNotificationTime()
            } else {
                Log.d("MainActivity", "Разрешение отклонено")
            }
        }
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
