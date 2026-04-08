package com.dreamjournal.journalofdream.ui.common

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Библиотека фонов для снов, локаций и образов.
 *
 * ID 0       = без фона
 * ID 1-15    = градиентные фоны (Compose Brush)
 * ID 201-215 = фото снов      (dreamfon1.jpg … dreamfon15.jpg)
 * ID 301-316 = фото локаций   (locationfone1.jpg … locationfone16.jpg)
 * ID 401-412 = фото образов   (characterfone1.jpg … characterfone12.jpg)
 *
 * Названия градиентов берутся из strings.xml (bg_1 … bg_15, bg_none).
 */
object DreamBackgrounds {

    enum class Type { DREAM, LOCATION, CHARACTER }

    // ─── Градиентные фоны ─────────────────────────────────────────────────────
    // name — ключ для stringResource, не сам текст
    data class Background(val id: Int, val nameKey: String, val brush: Brush)

    val gradients = listOf(

        // 0 — без фона
        Background(0, "bg_none",
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))),

        // 1 — Глубокий космос
        Background(1, "bg_1",
            Brush.verticalGradient(listOf(
                Color(0xFF000000),
                Color(0xFF0A0015),
                Color(0xFF0D0B2E),
                Color(0xFF1A1060)
            ))),

        // 2 — Пурпурная ночь
        Background(2, "bg_2",
            Brush.verticalGradient(listOf(
                Color(0xFF0D001A),
                Color(0xFF2D0B50),
                Color(0xFF4A1275),
                Color(0xFF1A003A)
            ))),

        // 3 — Золотой закат
        Background(3, "bg_3",
            Brush.verticalGradient(listOf(
                Color(0xFF0A0005),
                Color(0xFF3D0A00),
                Color(0xFF8B2500),
                Color(0xFFBF5A00),
                Color(0xFFD4870A)
            ))),

        // 4 — Лунный свет
        Background(4, "bg_4",
            Brush.verticalGradient(listOf(
                Color(0xFF000814),
                Color(0xFF001D3D),
                Color(0xFF003566),
                Color(0xFF0A1A3D),
                Color(0xFF1A2850)
            ))),

        // 5 — Изумрудный лес
        Background(5, "bg_5",
            Brush.verticalGradient(listOf(
                Color(0xFF000A05),
                Color(0xFF011E09),
                Color(0xFF073D14),
                Color(0xFF0D5C1E),
                Color(0xFF024A12)
            ))),

        // 6 — Алый туман
        Background(6, "bg_6",
            Brush.verticalGradient(listOf(
                Color(0xFF0A0000),
                Color(0xFF2A0000),
                Color(0xFF5C0A0A),
                Color(0xFF8B1010),
                Color(0xFF4A0505)
            ))),

        // 7 — Ледяная бездна
        Background(7, "bg_7",
            Brush.verticalGradient(listOf(
                Color(0xFF000814),
                Color(0xFF001F3F),
                Color(0xFF0D3B6E),
                Color(0xFF1A5F8A),
                Color(0xFF0A2A50)
            ))),

        // 8 — Звёздная пыль
        Background(8, "bg_8",
            Brush.radialGradient(listOf(
                Color(0xFF6B1FA8),
                Color(0xFF3D0E6B),
                Color(0xFF1A0540),
                Color(0xFF0A0020),
                Color(0xFF000008)
            ))),

        // 9 — Розовый туман
        Background(9, "bg_9",
            Brush.verticalGradient(listOf(
                Color(0xFF0D0010),
                Color(0xFF2E0535),
                Color(0xFF5C0A5A),
                Color(0xFF8B1570),
                Color(0xFF3A0540)
            ))),

        // 10 — Янтарный
        Background(10, "bg_10",
            Brush.verticalGradient(listOf(
                Color(0xFF050200),
                Color(0xFF1E0C00),
                Color(0xFF4A2000),
                Color(0xFF7B3D00),
                Color(0xFFA05A00)
            ))),

        // 11 — Северное сияние
        Background(11, "bg_11",
            Brush.linearGradient(listOf(
                Color(0xFF000814),
                Color(0xFF001A20),
                Color(0xFF003830),
                Color(0xFF005540),
                Color(0xFF003B60),
                Color(0xFF1A0042),
                Color(0xFF000814)
            ))),

        // 12 — Вулкан
        Background(12, "bg_12",
            Brush.verticalGradient(listOf(
                Color(0xFF000000),
                Color(0xFF0A0000),
                Color(0xFF1E0000),
                Color(0xFF4A0A00),
                Color(0xFF8B1A00),
                Color(0xFFBF3000)
            ))),

        // 13 — Тихий океан
        Background(13, "bg_13",
            Brush.verticalGradient(listOf(
                Color(0xFF000A14),
                Color(0xFF001A2E),
                Color(0xFF002A45),
                Color(0xFF003D5C),
                Color(0xFF001A30)
            ))),

        // 14 — Сиреневый
        Background(14, "bg_14",
            Brush.verticalGradient(listOf(
                Color(0xFF050010),
                Color(0xFF150030),
                Color(0xFF2E0B55),
                Color(0xFF4A1580),
                Color(0xFF2A0850)
            ))),

        // 15 — Туман
        Background(15, "bg_15",
            Brush.verticalGradient(listOf(
                Color(0xFF050510),
                Color(0xFF0D0D25),
                Color(0xFF1A1A40),
                Color(0xFF252550),
                Color(0xFF101030)
            ))),
    )

    // ─── Фото-фоны ────────────────────────────────────────────────────────────
    data class PhotoBackground(val id: Int)

    val dreamPhotos     = (1..15).map { i -> PhotoBackground(200 + i) }
    val locationPhotos  = (1..16).map { i -> PhotoBackground(300 + i) }
    val characterPhotos = (1..12).map { i -> PhotoBackground(400 + i) }

    // ─── API ──────────────────────────────────────────────────────────────────

    fun photosForType(type: Type): List<PhotoBackground> = when (type) {
        Type.DREAM     -> dreamPhotos
        Type.LOCATION  -> locationPhotos
        Type.CHARACTER -> characterPhotos
    }

    fun isPhoto(id: Int): Boolean = id >= 200

    fun getBrush(id: Int): Brush? =
        if (id == 0 || id >= 200) null
        else gradients.find { it.id == id }?.brush

    /** Ключ строкового ресурса для названия фона */
    fun getNameKey(id: Int): String = when {
        id == 0        -> "bg_none"
        id in 201..215 -> "bg_dream_photo"
        id in 301..316 -> "bg_location_photo"
        id in 401..412 -> "bg_character_photo"
        else           -> gradients.find { it.id == id }?.nameKey ?: "bg_none"
    }

    // Обратная совместимость
    fun getById(id: Int): Brush? = getBrush(id)
    fun getName(id: Int): String = getNameKey(id) // возвращает ключ — используй stringResource
    val all: List<Background> get() = gradients
}
