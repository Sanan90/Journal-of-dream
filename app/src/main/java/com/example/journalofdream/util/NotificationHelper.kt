package com.example.journalofdream.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.journalofdream.R
import com.example.journalofdream.ReminderReceiver
import java.util.Calendar

const val CHANNEL_ID = "dream_channel_id"
const val NOTIFICATION_ID = 123
const val ALARM_REQUEST_CODE = 456

/**
 * Создаём или обновляем канал (Android 8+).
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Dream Channel"
        val descriptionText = "Notifications for Dream Reminders"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Показываем уведомление сразу (если POST_NOTIFICATIONS разрешение на Android 13).
 */
fun showNotification(context: Context, title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val check = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        if (check != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Нет разрешения на уведомления
            return
        }
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(NOTIFICATION_ID, builder.build())
}

/**
 * Ставим однократный будильник на [hour]:[minute] (сегодня, если время прошло — на завтра).
 * Если система разрешает точные будильники — используем setExactAndAllowWhileIdle.
 * Иначе fallback на alarmManager.set(...).
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

        // Если время уже прошло, переносим на завтра
        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // Проверим, можем ли ставить точные будильники (Android 12+)
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true // ниже Android 12 нет ограничений
    }

    Log.d("scheduleDailyReminder", "hour=$hour, minute=$minute, canExact=$canScheduleExact")

    if (canScheduleExact) {
        // Можем использовать точный будильник
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("scheduleDailyReminder", "Using setExactAndAllowWhileIdle for ${calendar.time}")
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("scheduleDailyReminder", "Using setExact for ${calendar.time}")
        }
    } else {
        // Точные будильники запрещены => fallback на set() (не точный)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d("scheduleDailyReminder", "Using set() fallback for ${calendar.time}")
    }
}
