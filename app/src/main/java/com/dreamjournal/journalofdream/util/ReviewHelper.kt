package com.dreamjournal.journalofdream.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Управляет запросом оценки приложения через Google Play In-App Review API.
 *
 * Условия показа (все три должны выполняться):
 * 1. Пользователь записал хотя бы 1 сон
 * 2. Прошло минимум 3 дня с первого запуска приложения
 * 3. Запрос ещё не показывался этому пользователю
 *
 * Важно: Google Play сам решает, показывать ли диалог — API не гарантирует показ.
 * Это нормально и задумано самим Google. В debug-сборке диалог обычно не появляется.
 */
object ReviewHelper {

    private const val TAG = "ReviewHelper"
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_REVIEW_SHOWN = "review_shown"
    private const val KEY_FIRST_LAUNCH = "first_launch_millis"
    private const val MIN_DREAMS = 1
    private const val MIN_DAYS = 3L

    /**
     * Проверяет условия и при необходимости запускает In-App Review.
     * Вызывать при каждом открытии MainScreen — внутри стоит защита от повторного показа.
     *
     * @param activity  текущий Activity (нужен для Review API)
     * @param dreamsCount количество снов текущего пользователя
     */
    fun tryRequestReview(activity: Activity, dreamsCount: Int) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Уже показывали — не беспокоим повторно
        if (prefs.getBoolean(KEY_REVIEW_SHOWN, false)) return

        // Фиксируем дату первого запуска если ещё не зафиксирована
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        if (firstLaunch == 0L) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis()).apply()
            Log.d(TAG, "Первый запуск зафиксирован, ещё рано для запроса отзыва")
            return
        }

        // Условие 1: хотя бы один сон
        if (dreamsCount < MIN_DREAMS) {
            Log.d(TAG, "Недостаточно снов: $dreamsCount < $MIN_DREAMS")
            return
        }

        // Условие 2: прошло минимум 3 дня
        val daysSinceFirst = (System.currentTimeMillis() - firstLaunch) / (1000L * 60 * 60 * 24)
        if (daysSinceFirst < MIN_DAYS) {
            Log.d(TAG, "Прошло $daysSinceFirst дней, нужно $MIN_DAYS")
            return
        }

        // Все условия выполнены — запускаем In-App Review
        Log.d(TAG, "Условия выполнены (снов=$dreamsCount, дней=$daysSinceFirst), запускаем Review")

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                    // Помечаем как показанный — независимо от того, оставил ли пользователь отзыв.
                    // Google не даёт знать, что именно выбрал пользователь.
                    prefs.edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()
                    Log.d(TAG, "Review flow завершён")
                }
            } else {
                Log.e(TAG, "Не удалось запустить Review: ${task.exception?.message}")
            }
        }
    }
}
