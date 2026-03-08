package com.example.journalofdream.model

// ── Уровни ──────────────────────────────────────────────────────────────────
data class DreamLevel(
    val level: Int,
    val title: String,
    val emoji: String,
    val minDreams: Int,
    val maxDreams: Int,
    val color: String
)

val dreamLevels = listOf(
    DreamLevel(1, "Новичок",          "🌱", 0,    4,    "#78909C"),
    DreamLevel(2, "Мечтатель",        "🌙", 5,    14,   "#5C6BC0"),
    DreamLevel(3, "Исследователь",    "🔭", 15,   29,   "#7E57C2"),
    DreamLevel(4, "Путешественник",   "🌊", 30,   59,   "#26A69A"),
    DreamLevel(5, "Визионер",         "✨", 60,   99,   "#FFA726"),
    DreamLevel(6, "Мастер снов",      "🌟", 100,  199,  "#EF5350"),
    DreamLevel(7, "Архитектор грёз",  "🏛️", 200,  499,  "#AB47BC"),
    DreamLevel(8, "Хранитель снов",   "🔮", 500,  999,  "#EC407A"),
    DreamLevel(9, "Легенда",          "👑", 1000, Int.MAX_VALUE, "#FFD700")
)

fun getLevelForCount(count: Int): DreamLevel =
    dreamLevels.lastOrNull { count >= it.minDreams } ?: dreamLevels.first()

fun getNextLevel(current: DreamLevel): DreamLevel? =
    dreamLevels.getOrNull(current.level)

// ── Достижения ───────────────────────────────────────────────────────────────
// Сигнатура: dreams, streakDays, categories, locationsCount, maxDreamsInOneLoc, maxLocsInOneDream
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: (
        dreams: List<Dream>,
        streakDays: Int,
        categories: Int,
        locationsCount: Int,
        maxDreamsInLoc: Int,
        maxLocsInDream: Int
    ) -> Boolean
)

val allAchievements = listOf(

    // ── ПЕРВЫЕ ШАГИ ──────────────────────────────────────────────────────────
    Achievement(
        "first_dream", "Первый шаг", "Запишите первый сон", "🌱",
        { dreams, _, _, _, _, _ -> dreams.isNotEmpty() }
    ),
    Achievement(
        "first_location", "Первое место", "Создайте первую локацию", "📍",
        { _, _, _, locs, _, _ -> locs >= 1 }
    ),

    // ── КОЛИЧЕСТВО СНОВ ──────────────────────────────────────────────────────
    Achievement(
        "dreams_5", "Начало пути", "Запишите 5 снов", "📖",
        { dreams, _, _, _, _, _ -> dreams.size >= 5 }
    ),
    Achievement(
        "dreams_10", "Десятка", "Запишите 10 снов", "🔟",
        { dreams, _, _, _, _, _ -> dreams.size >= 10 }
    ),
    Achievement(
        "dreams_20", "Двадцатка", "Запишите 20 снов", "🌙",
        { dreams, _, _, _, _, _ -> dreams.size >= 20 }
    ),
    Achievement(
        "dreams_25", "Четверть сотни", "Запишите 25 снов", "✨",
        { dreams, _, _, _, _, _ -> dreams.size >= 25 }
    ),
    Achievement(
        "dreams_50", "Полста снов", "Запишите 50 снов", "⭐",
        { dreams, _, _, _, _, _ -> dreams.size >= 50 }
    ),
    Achievement(
        "dreams_100", "Сотня", "Запишите 100 снов", "💯",
        { dreams, _, _, _, _, _ -> dreams.size >= 100 }
    ),
    Achievement(
        "dreams_500", "Пятьсот снов", "Запишите 500 снов", "🔮",
        { dreams, _, _, _, _, _ -> dreams.size >= 500 }
    ),
    Achievement(
        "dreams_1000", "Легенда снов", "Запишите 1000 снов", "👑",
        { dreams, _, _, _, _, _ -> dreams.size >= 1000 }
    ),

    // ── ЛОКАЦИИ — КОЛИЧЕСТВО ─────────────────────────────────────────────────
    Achievement(
        "locations_5", "Исследователь мест", "Создайте 5 локаций", "🗺️",
        { _, _, _, locs, _, _ -> locs >= 5 }
    ),
    Achievement(
        "locations_10", "Картограф", "Создайте 10 локаций", "🧭",
        { _, _, _, locs, _, _ -> locs >= 10 }
    ),
    Achievement(
        "locations_20", "Хранитель мест", "Создайте 20 локаций", "🏛️",
        { _, _, _, locs, _, _ -> locs >= 20 }
    ),

    // ── ЛОКАЦИИ В ОДНОМ СНЕ ──────────────────────────────────────────────────
    Achievement(
        "locs_in_dream_2", "Двойной портал", "Привяжите 2 локации к одному сну", "🚪",
        { _, _, _, _, _, maxLocs -> maxLocs >= 2 }
    ),
    Achievement(
        "locs_in_dream_3", "Тройной переход", "Привяжите 3 локации к одному сну", "🌀",
        { _, _, _, _, _, maxLocs -> maxLocs >= 3 }
    ),
    Achievement(
        "locs_in_dream_5", "Сновидец-странник", "Привяжите 5 локаций к одному сну", "🌍",
        { _, _, _, _, _, maxLocs -> maxLocs >= 5 }
    ),

    // ── СНОВ В ОДНОЙ ЛОКАЦИИ ─────────────────────────────────────────────────
    Achievement(
        "dreams_in_loc_5", "Любимое место", "Запишите 5 снов в одной локации", "🏠",
        { _, _, _, _, maxDreams, _ -> maxDreams >= 5 }
    ),
    Achievement(
        "dreams_in_loc_10", "Родной уголок", "Запишите 10 снов в одной локации", "🏡",
        { _, _, _, _, maxDreams, _ -> maxDreams >= 10 }
    ),
    Achievement(
        "dreams_in_loc_20", "Постоянная локация", "Запишите 20 снов в одной локации", "🏰",
        { _, _, _, _, maxDreams, _ -> maxDreams >= 20 }
    ),

    // ── СТРИКИ ───────────────────────────────────────────────────────────────
    Achievement(
        "streak_3", "3 дня подряд", "Записывайте сны 3 дня подряд", "🔥",
        { _, streak, _, _, _, _ -> streak >= 3 }
    ),
    Achievement(
        "streak_7", "Неделя снов", "Записывайте сны 7 дней подряд", "🗓️",
        { _, streak, _, _, _, _ -> streak >= 7 }
    ),
    Achievement(
        "streak_14", "Две недели", "Записывайте сны 14 дней подряд", "📆",
        { _, streak, _, _, _, _ -> streak >= 14 }
    ),
    Achievement(
        "streak_28", "Лунный месяц", "Записывайте сны 28 дней подряд", "🌕",
        { _, streak, _, _, _, _ -> streak >= 28 }
    ),
    Achievement(
        "streak_30", "Месяц снов", "Записывайте сны 30 дней подряд", "🏆",
        { _, streak, _, _, _, _ -> streak >= 30 }
    ),
    Achievement(
        "streak_100", "Сто дней", "Записывайте сны 100 дней подряд", "💎",
        { _, streak, _, _, _, _ -> streak >= 100 }
    ),

    // ── КАТЕГОРИИ ────────────────────────────────────────────────────────────
    Achievement(
        "categories_3", "Разнообразие", "Используйте 3 разные категории", "🎨",
        { _, _, cats, _, _, _ -> cats >= 3 }
    ),
    Achievement(
        "categories_5", "Коллекционер", "Используйте 5 разных категорий", "🗂️",
        { _, _, cats, _, _, _ -> cats >= 5 }
    ),

    // ── ОСОБЫЕ ───────────────────────────────────────────────────────────────
    Achievement(
        "long_dream", "Подробный сон", "Запишите сон длиннее 500 символов", "📝",
        { dreams, _, _, _, _, _ -> dreams.any { it.content.length >= 500 } }
    ),
    Achievement(
        "marathon", "Марафон", "Запишите 3 сна за один день", "⚡",
        { dreams, _, _, _, _, _ -> dreams.groupBy { it.date }.any { it.value.size >= 3 } }
    ),
    Achievement(
        "night_owl", "Ночная сова", "Запишите 5 снов в один день", "🦉",
        { dreams, _, _, _, _, _ -> dreams.groupBy { it.date }.any { it.value.size >= 5 } }
    ),
    Achievement(
        "early_bird", "Ранняя пташка", "Запишите сон до 7 утра", "🌅",
        { dreams, _, _, _, _, _ ->
            dreams.any { dream ->
                try {
                    val hour = dream.time.split(":").firstOrNull()?.toIntOrNull() ?: return@any false
                    hour < 7 && dream.time.isNotBlank()
                } catch (e: Exception) { false }
            }
        }
    ),
    Achievement(
        "night_writer", "Полночник", "Запишите сон после 23:00", "🌃",
        { dreams, _, _, _, _, _ ->
            dreams.any { dream ->
                try {
                    val hour = dream.time.split(":").firstOrNull()?.toIntOrNull() ?: return@any false
                    hour >= 23 && dream.time.isNotBlank()
                } catch (e: Exception) { false }
            }
        }
    ),
    Achievement(
        "all_weekdays", "Все дни недели", "Запишите хотя бы по одному сну в каждый день недели", "📅",
        { dreams, _, _, _, _, _ ->
            val weekdays = mutableSetOf<Int>()
            dreams.forEach { dream ->
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val cal = java.util.Calendar.getInstance()
                    cal.time = sdf.parse(dream.date) ?: return@forEach
                    weekdays.add(cal.get(java.util.Calendar.DAY_OF_WEEK))
                } catch (e: Exception) {}
            }
            weekdays.containsAll(setOf(1, 2, 3, 4, 5, 6, 7))
        }
    ),
    Achievement(
        "nightmare", "Кошмар", "Запишите сон с категорией «Кошмар»", "😱",
        { dreams, _, _, _, _, _ -> dreams.any { it.category.equals("Кошмар", ignoreCase = true) } }
    ),
    Achievement(
        "comeback", "Возвращение", "Вернитесь после перерыва в 7+ дней", "🔁",
        { dreams, _, _, _, _, _ ->
            if (dreams.size < 2) return@Achievement false
            val sorted = dreams.mapNotNull { dream ->
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dream.date)
                } catch (e: Exception) { null }
            }.sorted()
            sorted.zipWithNext().any { (a, b) ->
                (b.time - a.time) / (1000L * 60 * 60 * 24) >= 7
            }
        }
    ),
    Achievement(
        "dreamer_week", "Активная неделя", "Запишите сны в 5 из 7 последних дней", "📊",
        { dreams, _, _, _, _, _ ->
            val dates = dreams.map { it.date }.toSet()
            val cal = java.util.Calendar.getInstance()
            var count = 0
            repeat(7) {
                val key = String.format("%04d-%02d-%02d",
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH))
                if (dates.contains(key)) count++
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            }
            count >= 5
        }
    ),
    Achievement(
        "lucky_77", "Везунчик", "Запишите ровно 77 снов", "🎲",
        { dreams, _, _, _, _, _ -> dreams.size == 77 }
    ),
    Achievement(
        "ghost", "Призрак", "Создайте сон без заголовка", "👻",
        { dreams, _, _, _, _, _ -> dreams.any { it.title.isBlank() } }
    )
)

// ── Вспомогательные функции ──────────────────────────────────────────────────
fun hexToColorSafe(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toInt(16) / 255f
        val g = clean.substring(2, 4).toInt(16) / 255f
        val b = clean.substring(4, 6).toInt(16) / 255f
        androidx.compose.ui.graphics.Color(r, g, b)
    } catch (e: Exception) { androidx.compose.ui.graphics.Color.Gray }
}

// ── Локализованные названия уровней и достижений ─────────────────────────────
fun getLevelTitle(context: android.content.Context, level: Int): String {
    val resId = context.resources.getIdentifier("level_$level", "string", context.packageName)
    return if (resId != 0) context.getString(resId) else dreamLevels.getOrNull(level - 1)?.title ?: ""
}

fun getAchievementTitle(context: android.content.Context, id: String): String {
    val key = "ach_${id.replace('-', '_')}_title"
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId)
    else allAchievements.find { it.id == id }?.title ?: id
}

fun getAchievementDesc(context: android.content.Context, id: String): String {
    val key = "ach_${id.replace('-', '_')}_desc"
    val resId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resId != 0) context.getString(resId)
    else allAchievements.find { it.id == id }?.description ?: ""
}
