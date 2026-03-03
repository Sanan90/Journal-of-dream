package com.example.journalofdream

import android.Manifest
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

        // 3) ИСПРАВЛЕНО: планируем уведомление один раз здесь в onCreate,
        // а не внутри Composable — так диалог не будет показываться повторно
        // при каждой рекомпозиции
        setupNotificationTime()

        setContent {
            AppTheme {
                val isSessionValid = remember { mutableStateOf<Boolean?>(null) }
                val dreamViewModel: DreamViewModel = viewModel()

                // Проверяем Firebase-сессию
                LaunchedEffect(Unit) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        currentUser.reload().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dreamViewModel.startSyncIfLoggedIn()
                                isSessionValid.value = true
                                Log.d("MainActivity", "Firebase-сессия валидна.")
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                isSessionValid.value = false
                                Log.e("MainActivity", "Сессия недействительна.")
                            }
                        }
                    } else {
                        isSessionValid.value = false
                        Log.d("MainActivity", "Нет авторизованного пользователя.")
                    }
                }

                when (isSessionValid.value) {
                    true -> JournalOfDreamApp()
                    false -> AuthScreen(
                        dreamViewModel = dreamViewModel,
                        onAuthSuccess = { isSessionValid.value = true },
                        onSkipAuth = { isSessionValid.value = true }
                    )
                    else -> BackgroundScreen()
                }
            }
        }
    }

    /**
     * Вызывается один раз в onCreate.
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
    }
}
