package com.example.journalofdream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.journalofdream.ui.JournalOfDreamApp
import com.example.journalofdream.util.createNotificationChannel
import com.example.journalofdream.util.scheduleDailyReminder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём канал для уведомлений
        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверяем POST_NOTIFICATIONS
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPostNotificationPermission()
            } else {
                scheduleDailyReminder(this, 8, 0)
            }
        } else {
            // < Android 13, не нужно POST_NOTIFICATIONS
            scheduleDailyReminder(this, 8, 0)
        }

        setContent {
            JournalOfDreamApp()
        }
    }

    /**
     * Запрашивает POST_NOTIFICATIONS с помощью нового API
     */
    private fun requestPostNotificationPermission() {
        // Регистрируем "лаунчер" для запроса разрешения
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "User granted POST_NOTIFICATIONS")
                scheduleDailyReminder(this, 8, 0)
            } else {
                Log.d("MainActivity", "User denied POST_NOTIFICATIONS")
            }
        }
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
