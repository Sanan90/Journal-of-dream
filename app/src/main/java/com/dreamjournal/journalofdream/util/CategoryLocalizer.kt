package com.dreamjournal.journalofdream.util

/**
 * Возвращает локализованное название категории.
 * Для встроенных категорий — переведённое название.
 * Для пользовательских — оригинальное.
 */
fun localizeCategory(
    name: String,
    noCategory: String,
    nightmares: String,
    lucid: String,
    plot: String,
    personal: String
): String = when (name.trim()) {
    "Без категории"  -> noCategory
    "Кошмары"        -> nightmares
    "Осознанные сны" -> lucid
    "Сюжетные сны"   -> plot
    "Личные сны"     -> personal
    else             -> name
}
