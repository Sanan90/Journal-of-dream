package com.dreamjournal.journalofdream.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
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

// ─── Старый канал (оставляем константу для обратной совместимости, но сам канал удаляем) ───
const val CHANNEL_ID_LEGACY = "dream_channel_id"

// ─── Новые каналы со звуками ───────────────────────────────────────────────────
// CHANNEL_ID_DREAM  — для напоминания записать сон и цитат (мягкий звук)
// CHANNEL_ID_REALITY — для проверки реальности (мистический звук)
const val CHANNEL_ID_DREAM   = "dream_channel_v2"
const val CHANNEL_ID_REALITY = "reality_check_channel_v2"

// Для обратной совместимости — старые вызовы showNotification без channelId
// будут использовать канал напоминаний
const val CHANNEL_ID = CHANNEL_ID_DREAM

const val NOTIFICATION_ID_REMINDER = 123
const val NOTIFICATION_ID_QUOTE    = 124
const val NOTIFICATION_ID_REALITY  = 125

const val ALARM_REQUEST_CODE           = 456
const val ALARM_REQUEST_CODE_QUOTE_10  = 457
const val ALARM_REQUEST_CODE_QUOTE_16  = 458
const val ALARM_REQUEST_CODE_QUOTE_22  = 459
const val ALARM_REQUEST_CODE_REALITY   = 460

// ─── Создание каналов уведомлений ──────────────────────────────────────────────
/**
 * Создаёт два канала с кастомными звуками.
 * Вызывать при старте приложения (в MainActivity.onCreate).
 *
 * Звуковые файлы:
 *   res/raw/sound_dream_reminder.ogg  — мягкий звук для напоминаний и цитат
 *   res/raw/sound_reality_check.ogg   — мистический звук для проверки реальности
 *
 * ВАЖНО: звук канала задаётся один раз при создании.
 * Если канал уже существует — звук не изменится. Поэтому у каналов новые ID (v2).
 */
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Удаляем старый канал без звука
        manager.deleteNotificationChannel(CHANNEL_ID_LEGACY)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        // Канал для напоминаний и цитат — мягкий звук
        val dreamSoundUri = Uri.parse(
            "android.resource://${context.packageName}/${R.raw.sound_dream_reminder}"
        )
        val dreamChannel = NotificationChannel(
            CHANNEL_ID_DREAM,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setSound(dreamSoundUri, audioAttributes)
        }

        // Канал для проверки реальности — мистический звук
        val realitySoundUri = Uri.parse(
            "android.resource://${context.packageName}/${R.raw.sound_reality_check}"
        )
        val realityChannel = NotificationChannel(
            CHANNEL_ID_REALITY,
            context.getString(R.string.reality_check_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.reality_check_channel_description)
            setSound(realitySoundUri, audioAttributes)
        }

        manager.createNotificationChannel(dreamChannel)
        manager.createNotificationChannel(realityChannel)
    }
}

// ─── Проверки разрешений ────────────────────────────────────────────────────────
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

fun canUseExactAlarms(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
}

// ─── Открытие системных настроек ───────────────────────────────────────────────
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

// ─── Показ уведомлений ─────────────────────────────────────────────────────────
fun showNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = NOTIFICATION_ID_REMINDER,
    channelId: String = CHANNEL_ID_DREAM
) {
    createNotificationChannel(context)

    if (!areNotificationsAllowed(context)) {
        Log.d("NotificationHelper", "Уведомления системно запрещены или нет разрешения")
        return
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pendingIntent = launchIntent?.let {
        PendingIntent.getActivity(
            context, 0, it,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    val notification = NotificationCompat.Builder(context, channelId)
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

/**
 * Показ уведомления для проверки реальности — использует отдельный канал с мистическим звуком.
 */
fun showRealityCheckNotification(context: Context, title: String, message: String) {
    showNotification(
        context = context,
        title = title,
        message = message,
        notificationId = NOTIFICATION_ID_REALITY,
        channelId = CHANNEL_ID_REALITY
    )
}

// ─── Планирование уведомлений ──────────────────────────────────────────────────
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
    val startHour   = prefs.getInt("reality_check_start_hour", 8)
    val startMinute = prefs.getInt("reality_check_start_minute", 0)
    val endHour     = prefs.getInt("reality_check_end_hour", 22)
    val endMinute   = prefs.getInt("reality_check_end_minute", 0)

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

// ─── Внутренние утилиты ────────────────────────────────────────────────────────
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
        context, requestCode, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val canScheduleExact = canUseExactAlarms(context)
    Log.d("NotificationHelper", "Планируем alarm requestCode=$requestCode at=${calendar.time} exact=$canScheduleExact")
    if (canScheduleExact) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
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
        context, requestCode, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val canScheduleExact = canUseExactAlarms(context)
    Log.d("NotificationHelper", "Планируем alarm requestCode=$requestCode at=${java.util.Date(triggerAtMillis)} exact=$canScheduleExact")
    if (canScheduleExact) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}

private fun <T> cancelAlarm(context: Context, requestCode: Int, receiverClass: Class<T>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, receiverClass)
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
    Log.d("NotificationHelper", "Отменён alarm requestCode=$requestCode")
}
