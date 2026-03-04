package com.example.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_10
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_16
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_22
import com.example.journalofdream.util.NOTIFICATION_ID_QUOTE
import com.example.journalofdream.util.getRandomQuote
import com.example.journalofdream.util.scheduleAlarmByRequestCode
import com.example.journalofdream.util.showNotification
import java.util.Calendar

class QuoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)

        if (quotesEnabled) {
            val quote = getRandomQuote()
            showNotification(
                context,
                title = "🌙 Осознанные сновидения",
                message = quote,
                notificationId = NOTIFICATION_ID_QUOTE
            )
            Log.d("QuoteReceiver", "Цитата отправлена")
        }

        // Перепланируем на следующий день
        val requestCode = intent.getIntExtra("request_code", -1)
        val hour = when (requestCode) {
            ALARM_REQUEST_CODE_QUOTE_10 -> 10
            ALARM_REQUEST_CODE_QUOTE_16 -> 16
            ALARM_REQUEST_CODE_QUOTE_22 -> 22
            else -> return
        }
        scheduleAlarmByRequestCode(context, hour, 0, requestCode)
    }
}
