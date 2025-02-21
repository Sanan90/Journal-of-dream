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
import com.example.journalofdream.util.REMINDER_HOUR
import com.example.journalofdream.util.REMINDER_MINUTE
import com.example.journalofdream.util.createNotificationChannel
import com.example.journalofdream.util.scheduleDailyReminder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём канал уведомлений
        createNotificationChannel(this)

        // Если Android 13 или выше, проверяем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPostNotificationPermission()
            } else {
                // Планируем ежедневное напоминание на 8:00
                scheduleDailyReminder(this, REMINDER_HOUR, REMINDER_MINUTE)
            }
        } else {
            // Для версий ниже Android 13 разрешение не требуется
            scheduleDailyReminder(this, REMINDER_HOUR, REMINDER_MINUTE)
        }

        setContent {
            JournalOfDreamApp()
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
                scheduleDailyReminder(this, REMINDER_HOUR, REMINDER_MINUTE)
            } else {
                Log.d("MainActivity", "Разрешение отклонено")
            }
        }
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
