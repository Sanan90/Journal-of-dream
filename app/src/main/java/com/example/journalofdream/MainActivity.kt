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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.createNotificationChannel
import com.example.journalofdream.viewmodel.DreamViewModel
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Создаём канал уведомлений (для Android 8+)
        createNotificationChannel(this)

        // 2) Проверяем разрешение POST_NOTIFICATIONS (Android 13+)
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
            // < Android 13
            handleNotificationTime()
        }

        // 3) Устанавливаем Compose-контент
        setContent {
            // Получаем DreamViewModel, чтобы при запуске приложения,
            // если пользователь уже авторизован (FirebaseAuth), запустить синх.
            val dreamViewModel: DreamViewModel = viewModel()

            // Если user уже logged in, стартуем sync (Firestore -> Room)
            dreamViewModel.startSyncIfLoggedIn()

            JournalOfDreamApp()
        }
    }

    /**
     * Если пользователь ещё не выбрал время уведомлений, показываем TimePickerDialog,
     * иначе планируем уведомление (scheduleDailyReminder).
     */
    private fun handleNotificationTime() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.contains("notification_hour") || !prefs.contains("notification_minute")) {
            // Время не выбрано - предлагаем выбрать
            showTimePickerDialog()
        } else {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Уведомление запланировано на $hour:$minute")
        }
    }

    /**
     * Показываем диалог выбора времени и сохраняем результат,
     * затем планируем уведомление.
     */
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hourOfDay, minute ->
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            prefs.putInt("notification_hour", hourOfDay)
            prefs.putInt("notification_minute", minute)
            prefs.apply()

            scheduleDailyReminder(this, hourOfDay, minute)
            Log.d("MainActivity", "Пользователь выбрал время: $hourOfDay:$minute")

        }, currentHour, currentMinute, true).apply {
            setTitle("В какое время напомнить о записи сна?\n" +
                    "Выберите время, когда вы обычно просыпаетесь, пока сон еще свеж в памяти.")
            show()
        }
    }

    /**
     * Запрашиваем разрешение POST_NOTIFICATIONS на Android 13+.
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
