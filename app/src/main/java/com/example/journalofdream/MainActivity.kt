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
 * Полный рабочий пример MainActivity:
 * 1. Создаёт канал уведомлений.
 * 2. Запрашивает разрешение POST_NOTIFICATIONS (Android 13+).
 * 3. Проверяет валидность Firebase-сессии (reload),
 *    если авторизован — загружает JournalOfDreamApp(),
 *    если нет — показывает AuthScreen.
 * 4. При первом запуске предлагает выбрать время напоминания.
 */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 1) Создаём канал уведомлений (для Android 8+)
        createNotificationChannel(this)

        // 2) Запрашиваем разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем у пользователя
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            } else {
                // Уведомления уже разрешены
                Log.d("MainActivity", "Уведомления разрешены")
            }
        }

        // 3) Устанавливаем Compose-контент
        setContent {
            AppTheme {

                // Состояние, отслеживающее результат проверки сессии
                val isSessionValid = remember { mutableStateOf<Boolean?>(null) }

                // ViewModel для работы со снами (Room + Firestore)
                val dreamViewModel: DreamViewModel = viewModel()

                // При первом запуске композабл (после onCreate) проверяем актуальность сессии
                LaunchedEffect(Unit) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        // reload() проверит, не устарела ли сессия
                        currentUser.reload().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Сессия действительна
                                dreamViewModel.startSyncIfLoggedIn() // если пользователь авторизован, подключаем sync
                                isSessionValid.value = true
                                Log.d("Auth", "Сессия действительна.")
                            } else {
                                // Сессия устарела/повреждена
                                FirebaseAuth.getInstance().signOut()
                                isSessionValid.value = false
                                Log.e("Auth", "Сессия устарела, выполняем выход.")
                            }
                        }
                    } else {
                        // Пользователь не авторизован
                        isSessionValid.value = false
                        Log.d("Auth", "Пользователь не авторизован.")
                    }
                }

                // UI в зависимости от того, есть ли валидная сессия
                when (isSessionValid.value) {
                    // Если true — пользователя пустим сразу в основное приложение
                    true -> {
                        // Проверяем или создаём ежедневное напоминание
                        NotificationTimeManager(this@MainActivity)
                        JournalOfDreamApp()
                    }
                    // Если false — показываем экран входа
                    false -> {
                        // Аналогично проверяем уведомления (гость может захотеть иметь локальные уведомления)
                        NotificationTimeManager(this@MainActivity)
                        AuthScreen(
                            dreamViewModel = dreamViewModel,
                            onAuthSuccess = {
                                // Когда пользователь вошёл, сбрасываем флаг
                                isSessionValid.value = true
                            },
                            onSkipAuth = {
                                // Пользователь выбрал гостевой режим
                                isSessionValid.value = true // Пустим на главный экран, но без синхронизации
                            }
                        )
                    }
                    // Пока не пришёл ответ от reload()
                    else -> {
                        BackgroundScreen()
                    }
                }
            }
        }
    }
}

/**
 * Утилитная функция, которая проверяет SharedPreferences, и при первом запуске
 * предлагает пользователю выбрать время уведомления.
 * Если уже выбрано — просто перепланирует.
 */
@Composable
fun NotificationTimeManager(activity: ComponentActivity) {
    LaunchedEffect(Unit) {
        val prefs = activity.getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Проверка, установлено ли уже время уведомления
        if (!prefs.contains("notification_hour") || !prefs.contains("notification_minute")) {
            // Если не указано, показываем TimePicker
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
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
            // Если уже есть, просто планируем его
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(activity, hour, minute)
            Log.d("MainActivity", "Уведомление уже настроено на $hour:$minute")
        }
    }
}
