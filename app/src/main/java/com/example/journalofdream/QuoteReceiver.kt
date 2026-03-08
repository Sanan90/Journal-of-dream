package com.example.journalofdream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_10
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_16
import com.example.journalofdream.util.ALARM_REQUEST_CODE_QUOTE_22
import com.example.journalofdream.util.NOTIFICATION_ID_QUOTE
import com.example.journalofdream.util.LocaleHelper
import com.example.journalofdream.util.getRandomQuote
import com.example.journalofdream.util.scheduleAlarmByRequestCode
import com.example.journalofdream.util.showNotification
import com.google.firebase.firestore.FirebaseFirestore

class QuoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val quotesEnabled = prefs.getBoolean("motivational_quotes", true)
        if (!quotesEnabled) return

        val requestCode = intent.getIntExtra("request_code", -1)
        val hour = when (requestCode) {
            ALARM_REQUEST_CODE_QUOTE_10 -> 10
            ALARM_REQUEST_CODE_QUOTE_16 -> 16
            ALARM_REQUEST_CODE_QUOTE_22 -> 22
            else -> return
        }

        // Применяем язык приложения к контексту
        val localizedContext = LocaleHelper.applyLanguage(context)
        val notifTitle = localizedContext.getString(R.string.notif_quote_title)

        // Пробуем загрузить цитату из Firestore
        FirebaseFirestore.getInstance()
            .collection("quotes")
            .get()
            .addOnSuccessListener { snapshot ->
                // Цитаты из Firestore показываем как есть (admin сам добавляет на нужном языке)
                // Встроенные цитаты — берём на языке приложения
                val firestoreQuotes = snapshot.documents.mapNotNull { it.getString("text") }
                val quote = if (firestoreQuotes.isNotEmpty()) firestoreQuotes.random()
                            else getRandomQuote(localizedContext)
                showNotification(
                    context,
                    title = notifTitle,
                    message = quote,
                    notificationId = NOTIFICATION_ID_QUOTE
                )
                Log.d("QuoteReceiver", "Цитата отправлена: $quote")
            }
            .addOnFailureListener {
                // Нет сети — используем встроенные цитаты на языке приложения
                val quote = getRandomQuote(localizedContext)
                showNotification(
                    context,
                    title = notifTitle,
                    message = quote,
                    notificationId = NOTIFICATION_ID_QUOTE
                )
                Log.d("QuoteReceiver", "Fallback цитата: $quote")
            }

        // Перепланируем на следующий день
        scheduleAlarmByRequestCode(context, hour, 0, requestCode)
    }
}
