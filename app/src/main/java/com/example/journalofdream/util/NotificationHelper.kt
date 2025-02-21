package com.example.journalofdream.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.journalofdream.R
import com.example.journalofdream.ReminderReceiver
import java.util.Calendar

// Константы для канала уведомлений и напоминания
const val CHANNEL_ID = "dream_channel_id"
const val NOTIFICATION_ID = 123
const val ALARM_REQUEST_CODE = 456

// Фиксированное время напоминания: 8:00 утра
const val REMINDER_HOUR = 22
const val REMINDER_MINUTE = 49

/**
 * Создаёт канал уведомлений (для Android 8.0+).
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Dream Channel"
        val descriptionText = "Уведомления для напоминаний о записи сна"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Показывает уведомление.
 * Если Android 13+, проверяет наличие разрешения POST_NOTIFICATIONS.
 */
fun showNotification(context: Context, title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val check = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        if (check != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Если нет разрешения, уведомление не показывается
            Log.d("NotificationHelper", "Нет разрешения POST_NOTIFICATIONS")
            return
        }
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Замените на свой значок, если нужно
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(NOTIFICATION_ID, builder.build())
}

/**
 * Планирует однократный будильник на указанное время (час и минута).
 * Если выбранное время уже прошло – планирует на следующий день.
 * При срабатывании будет вызван ReminderReceiver.
 */
fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // Если выбранное время уже прошло сегодня – переносим на следующий день
        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Проверяем возможность установки точных будильников (для Android 12+)
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    Log.d("scheduleDailyReminder", "Планирование на время: ${calendar.time}, точный режим: $canScheduleExact")

    if (canScheduleExact) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } else {
        // Если точные будильники недоступны, используем set()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
