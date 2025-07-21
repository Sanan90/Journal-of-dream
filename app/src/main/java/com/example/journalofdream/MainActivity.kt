package com.example.journalofdream

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.ui.auth.AuthScreen
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.theme.AppTheme
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.createNotificationChannel
import com.example.journalofdream.viewmodel.DreamViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

/**
 * Главная Activity приложения.
 * 1) Создаёт канал уведомлений (Android 8+).
 * 2) Запрашивает разрешение POST_NOTIFICATIONS (Android 13+), если нужно.
 * 3) Проверяет актуальность Firebase-сессии (reload).
 * 4) Если пользователь валиден – запускает JournalOfDreamApp,
 *    иначе показывает AuthScreen. (В зависимости от реализации)
 * 5) Показывает TimePickerDialog при первом запуске, чтобы пользователь настроил уведомление.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Создаём канал уведомлений
        createNotificationChannel(this)

        // 2) Запрашиваем разрешение на уведомления (Android 13+), если оно не выдано
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            } else {
                Log.d("MainActivity", "Уведомления уже разрешены пользователем.")
            }
        }

        setContent {
            AppTheme {

                // Состояние, проверяющее валидность сессии
                val isSessionValid = remember { mutableStateOf<Boolean?>(null) }

                // Пример: создаём один DreamViewModel (если нужно)
                val dreamViewModel: DreamViewModel = viewModel()

                // 3) При первом запуске проверяем Firebase-сессию
                LaunchedEffect(Unit) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        // reload() проверит, не просрочена ли
                        currentUser.reload().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Сессия норм
                                dreamViewModel.startSyncIfLoggedIn()
                                isSessionValid.value = true
                                Log.d("MainActivity", "Firebase-сессия валидна.")
                            } else {
                                // Сессия недействительна
                                FirebaseAuth.getInstance().signOut()
                                isSessionValid.value = false
                                Log.e("MainActivity", "Сессия недействительна, выходим.")
                            }
                        }
                    } else {
                        // Никто не авторизован
                        isSessionValid.value = false
                        Log.d("MainActivity", "Нет авторизованного пользователя.")
                    }
                }

                // 4) В зависимости от флага isSessionValid – показываем AuthScreen или JournalOfDreamApp
                when (isSessionValid.value) {
                    true -> {
                        // 5) Предлагаем выбрать время уведомления (или планируем уже выбранное):
                        NotificationTimeManager(this@MainActivity)

                        // Запуск основного приложения (NavHost с main, auth, etc.)
                        JournalOfDreamApp()
                    }
                    false -> {
                        // Предлагаем выбрать время уведомления и показываем AuthScreen
                        NotificationTimeManager(this@MainActivity)
                        AuthScreen(
                            dreamViewModel = dreamViewModel,
                            onAuthSuccess = {
                                isSessionValid.value = true
                            },
                            onSkipAuth = {
                                // гость
                                isSessionValid.value = true
                            }
                        )
                    }
                    else -> {
                        // Ещё нет ответа (reload в процессе) – Loading
                        BackgroundScreen()
                    }
                }
            }
        }
    }
}

/**
 * Утилита: проверяем SharedPrefs, если нет notification_hour/minute –
 * показываем TimePickerDialog. Иначе планируем ежедневное уведомление.
 */
@Composable
fun NotificationTimeManager(activity: ComponentActivity) {
    LaunchedEffect(Unit) {
        val prefs = activity.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasHour = prefs.contains("notification_hour")
        val hasMinute = prefs.contains("notification_minute")

        if (!hasHour || !hasMinute) {
            // Показываем TimePicker
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            android.app.TimePickerDialog(
                activity,
                { _, chosenHour, chosenMinute ->
                    prefs.edit()
                        .putInt("notification_hour", chosenHour)
                        .putInt("notification_minute", chosenMinute)
                        .apply()

                    scheduleDailyReminder(activity, chosenHour, chosenMinute)
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
            scheduleDailyReminder(activity, hour, minute)
            Log.d("MainActivity", "Уведомление уже настроено на $hour:$minute")
        }
    }
}
