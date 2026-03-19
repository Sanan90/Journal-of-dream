package com.dreamjournal.journalofdream.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ReminderReceiver
import com.dreamjournal.journalofdream.QuoteReceiver
import com.dreamjournal.journalofdream.RealityCheckReceiver
import java.util.Calendar

const val CHANNEL_ID = "dream_channel_id"
const val NOTIFICATION_ID_REMINDER = 123
const val NOTIFICATION_ID_QUOTE = 124
const val ALARM_REQUEST_CODE = 456
const val ALARM_REQUEST_CODE_QUOTE_10 = 457
const val ALARM_REQUEST_CODE_QUOTE_16 = 458
const val ALARM_REQUEST_CODE_QUOTE_22 = 459
const val ALARM_REQUEST_CODE_REALITY = 460
const val NOTIFICATION_ID_REALITY = 125

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

fun areNotificationsAllowed(context: Context): Boolean {
    val manager = NotificationManagerCompat.from(context)
    val channelEnabled = manager.areNotificationsEnabled()
    val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true
    return channelEnabled && runtimeGranted
}

fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun openExactAlarmSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    context.startActivity(intent)
}

fun showNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = NOTIFICATION_ID_REMINDER
) {
    createNotificationChannel(context)

    if (!areNotificationsAllowed(context)) {
        Log.d("NotificationHelper", "Уведомления системно запрещены или не выдано разрешение")
        return
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

fun scheduleRealityCheck(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val enabled = prefs.getBoolean("reality_check_enabled", false)
    if (!enabled) {
        cancelRealityCheck(context)
        return
    }

    val intervalMinutes = prefs.getInt("reality_check_interval_minutes", 60)
    val startHour = prefs.getInt("reality_check_start_hour", 8)
    val startMinute = prefs.getInt("reality_check_start_minute", 0)
    val endHour = prefs.getInt("reality_check_end_hour", 22)
    val endMinute = prefs.getInt("reality_check_end_minute", 0)

    val triggerAtMillis = computeNextRealityCheckTriggerMillis(
        intervalMinutes = intervalMinutes,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute
    )

    scheduleAlarmAtMillis(
        context = context,
        triggerAtMillis = triggerAtMillis,
        requestCode = ALARM_REQUEST_CODE_REALITY,
        receiverClass = RealityCheckReceiver::class.java
    )
}

fun cancelRealityCheck(context: Context) {
    cancelAlarm(context, ALARM_REQUEST_CODE_REALITY, RealityCheckReceiver::class.java)
}

private fun computeNextRealityCheckTriggerMillis(
    intervalMinutes: Int,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    nowMillis: Long = System.currentTimeMillis()
): Long {
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val startToday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, startHour)
        set(Calendar.MINUTE, startMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endToday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, endHour)
        set(Calendar.MINUTE, endMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (now.timeInMillis <= startToday.timeInMillis) return startToday.timeInMillis

    if (now.timeInMillis >= endToday.timeInMillis) {
        return Calendar.getInstance().apply {
            timeInMillis = startToday.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    }

    val minutesSinceStart = ((now.timeInMillis - startToday.timeInMillis) / 60000L).toInt()
    val nextStep = (minutesSinceStart / intervalMinutes) + 1
    val nextTime = Calendar.getInstance().apply {
        timeInMillis = startToday.timeInMillis
        add(Calendar.MINUTE, nextStep * intervalMinutes)
    }

    return if (nextTime.timeInMillis > endToday.timeInMillis) {
        Calendar.getInstance().apply {
            timeInMillis = startToday.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    } else {
        nextTime.timeInMillis
    }
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
    Log.d(
        "NotificationHelper",
        "Планируем alarm requestCode=$requestCode receiver=${receiverClass.simpleName} at=${calendar.time} exact=$canScheduleExact"
    )

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

private fun <T> scheduleAlarmAtMillis(
    context: Context,
    triggerAtMillis: Long,
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

    val canScheduleExact = canUseExactAlarms(context)
    Log.d(
        "NotificationHelper",
        "Планируем alarm requestCode=$requestCode receiver=${receiverClass.simpleName} at=${java.util.Date(triggerAtMillis)} exact=$canScheduleExact"
    )

    if (canScheduleExact) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
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
    Log.d("NotificationHelper", "Отменён alarm requestCode=$requestCode receiver=${receiverClass.simpleName}")
}