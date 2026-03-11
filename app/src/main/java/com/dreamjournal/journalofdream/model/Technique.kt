package com.dreamjournal.journalofdream.model

// Модель техники осознанных снов (хранится в Firestore)
// name/description — оригинал (ru), name_XX/description_XX — переводы
data class Technique(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val source: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0,
    val createdAt: Long = 0L,
    val commentsCount: Int = 0,
    // Переводы названий
    val name_en: String = "",
    val name_az: String = "",
    val name_de: String = "",
    val name_fr: String = "",
    val name_es: String = "",
    val name_pt: String = "",
    val name_tr: String = "",
    val name_ar: String = "",
    val name_ko: String = "",
    val name_ja: String = "",
    val name_nl: String = "",
    val name_pl: String = "",
    val name_it: String = "",
    val name_zh: String = "",
    // Переводы описаний
    val description_en: String = "",
    val description_az: String = "",
    val description_de: String = "",
    val description_fr: String = "",
    val description_es: String = "",
    val description_pt: String = "",
    val description_tr: String = "",
    val description_ar: String = "",
    val description_ko: String = "",
    val description_ja: String = "",
    val description_nl: String = "",
    val description_pl: String = "",
    val description_it: String = "",
    val description_zh: String = "",
    // Украинский и казахский
    val name_uk: String = "",
    val name_kk: String = "",
    val description_uk: String = "",
    val description_kk: String = "",
) {
    // Возвращает название на нужном языке, фолбэк на ru
    fun localizedName(lang: String): String = when (lang) {
        "en" -> name_en.ifBlank { name }
        "az" -> name_az.ifBlank { name }
        "de" -> name_de.ifBlank { name }
        "fr" -> name_fr.ifBlank { name }
        "es" -> name_es.ifBlank { name }
        "pt" -> name_pt.ifBlank { name }
        "tr" -> name_tr.ifBlank { name }
        "ar" -> name_ar.ifBlank { name }
        "ko" -> name_ko.ifBlank { name }
        "ja" -> name_ja.ifBlank { name }
        "nl" -> name_nl.ifBlank { name }
        "pl" -> name_pl.ifBlank { name }
        "it" -> name_it.ifBlank { name }
        "zh" -> name_zh.ifBlank { name }
        "uk" -> name_uk.ifBlank { name }
        "kk" -> name_kk.ifBlank { name }
        else -> name
    }

    fun localizedDescription(lang: String): String = when (lang) {
        "en" -> description_en.ifBlank { description }
        "az" -> description_az.ifBlank { description }
        "de" -> description_de.ifBlank { description }
        "fr" -> description_fr.ifBlank { description }
        "es" -> description_es.ifBlank { description }
        "pt" -> description_pt.ifBlank { description }
        "tr" -> description_tr.ifBlank { description }
        "ar" -> description_ar.ifBlank { description }
        "ko" -> description_ko.ifBlank { description }
        "ja" -> description_ja.ifBlank { description }
        "nl" -> description_nl.ifBlank { description }
        "pl" -> description_pl.ifBlank { description }
        "it" -> description_it.ifBlank { description }
        "zh" -> description_zh.ifBlank { description }
        "uk" -> description_uk.ifBlank { description }
        "kk" -> description_kk.ifBlank { description }
        else -> description
    }
}
