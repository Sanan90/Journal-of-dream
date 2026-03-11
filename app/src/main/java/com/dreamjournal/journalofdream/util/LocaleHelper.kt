package com.dreamjournal.journalofdream.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "app_language"

    // Языки которые поддерживает приложение
    data class AppLanguage(
        val code: String,      // BCP-47 код для Locale
        val displayName: String, // Название на самом языке (для носителя)
        val flag: String
    )

    val supportedLanguages = listOf(
        AppLanguage("system", "Системный", "🌐"),
        AppLanguage("ru", "Русский", "🇷🇺"),
        AppLanguage("en", "English", "🇬🇧"),
        AppLanguage("az", "Azərbaycan", "🇦🇿"),
        AppLanguage("de", "Deutsch", "🇩🇪"),
        AppLanguage("fr", "Français", "🇫🇷"),
        AppLanguage("es", "Español", "🇪🇸"),
        AppLanguage("pt", "Português", "🇵🇹"),
        AppLanguage("tr", "Türkçe", "🇹🇷"),
        AppLanguage("ar", "العربية", "🇸🇦"),
        AppLanguage("ko", "한국어", "🇰🇷"),
        AppLanguage("ja", "日本語", "🇯🇵"),
        AppLanguage("nl", "Nederlands", "🇳🇱"),
        AppLanguage("pl", "Polski", "🇵🇱"),
        AppLanguage("it", "Italiano", "🇮🇹"),
        AppLanguage("zh", "中文", "🇨🇳"),
        AppLanguage("uk", "Українська", "🇺🇦"),
        AppLanguage("kk", "Қазақша", "🇰🇿")
    )

    // Получить сохранённый код языка (или "system")
    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "system") ?: "system"
    }

    // Сохранить выбранный язык
    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    // Применить язык к контексту (вызывается в attachBaseContext)
    fun applyLanguage(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        if (languageCode == "system") return context

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    // Найти язык по коду
    fun findLanguage(code: String): AppLanguage {
        return supportedLanguages.find { it.code == code } ?: supportedLanguages[0]
    }
}
