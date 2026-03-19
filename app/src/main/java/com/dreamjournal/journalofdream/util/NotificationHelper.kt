package com.dreamjournal.journalofdream.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ReminderReceiver
import com.dreamjournal.journalofdream.QuoteReceiver
import java.util.Calendar

const val CHANNEL_ID = "dream_channel_id"
const val NOTIFICATION_ID_REMINDER = 123
const val NOTIFICATION_ID_QUOTE = 124
const val ALARM_REQUEST_CODE = 456
const val ALARM_REQUEST_CODE_QUOTE_10 = 457
const val ALARM_REQUEST_CODE_QUOTE_16 = 458
const val ALARM_REQUEST_CODE_QUOTE_22 = 459

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

fun showNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = NOTIFICATION_ID_REMINDER
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val check = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        if (check != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.d("NotificationHelper", "Нет разрешения POST_NOTIFICATIONS")
            return
        }
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pendingIntent = launchIntent?.let {
        PendingIntent.getActivity(
            context,
            0,
            it,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
}

fun canUseExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
}

fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
    scheduleAlarm(context, hour, minute, ALARM_REQUEST_CODE, ReminderReceiver::class.java)
}

fun cancelDailyReminder(context: Context) {
    cancelAlarm(context, ALARM_REQUEST_CODE, ReminderReceiver::class.java)
}

fun scheduleQuoteAlarms(context: Context) {
    scheduleAlarm(context, 10, 0, ALARM_REQUEST_CODE_QUOTE_10, QuoteReceiver::class.java)
    scheduleAlarm(context, 16, 0, ALARM_REQUEST_CODE_QUOTE_16, QuoteReceiver::class.java)
    scheduleAlarm(context, 22, 0, ALARM_REQUEST_CODE_QUOTE_22, QuoteReceiver::class.java)
}

fun cancelQuoteAlarms(context: Context) {
    cancelAlarm(context, ALARM_REQUEST_CODE_QUOTE_10, QuoteReceiver::class.java)
    cancelAlarm(context, ALARM_REQUEST_CODE_QUOTE_16, QuoteReceiver::class.java)
    cancelAlarm(context, ALARM_REQUEST_CODE_QUOTE_22, QuoteReceiver::class.java)
}

fun scheduleAlarmByRequestCode(context: Context, hour: Int, minute: Int, requestCode: Int) {
    scheduleAlarm(context, hour, minute, requestCode, QuoteReceiver::class.java)
}

private fun <T> scheduleAlarm(
    context: Context,
    hour: Int,
    minute: Int,
    requestCode: Int,
    receiverClass: Class<T>
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass).apply {
        putExtra("request_code", requestCode)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val canScheduleExact = canUseExactAlarms(context)

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}

private fun <T> cancelAlarm(context: Context, requestCode: Int, receiverClass: Class<T>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}