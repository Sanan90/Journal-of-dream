package com.dreamjournal.journalofdream

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dreamjournal.journalofdream.ui.JournalOfDreamApp
import com.dreamjournal.journalofdream.ui.theme.AppTheme
import com.dreamjournal.journalofdream.util.LocaleHelper
import com.dreamjournal.journalofdream.util.createNotificationChannel
import com.dreamjournal.journalofdream.util.scheduleDailyReminder
import com.dreamjournal.journalofdream.util.scheduleQuoteAlarms

class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(this)
        requestPostNotificationsPermission()
        restoreScheduledNotifications()

        setContent {
            AppTheme {
                JournalOfDreamApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rescheduleIfExactAlarmGranted()
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    private fun restoreScheduledNotifications() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)

        if (notificationsEnabled &&
            prefs.contains("notification_hour") &&
            prefs.contains("notification_minute")
        ) {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Уведомление восстановлено на $hour:$minute")
        }

        if (quotesEnabled) {
            scheduleQuoteAlarms(this)
        }
    }

    private fun rescheduleIfExactAlarmGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) return

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)

        if (notificationsEnabled &&
            prefs.contains("notification_hour") &&
            prefs.contains("notification_minute")
        ) {
            val hour = prefs.getInt("notification_hour", 8)
            val minute = prefs.getInt("notification_minute", 0)
            scheduleDailyReminder(this, hour, minute)
            Log.d("MainActivity", "Точный alarm перепланирован на $hour:$minute")
        }

        if (quotesEnabled) {
            scheduleQuoteAlarms(this)
        }
    }
}