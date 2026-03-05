package com.example.journalofdream.model

// Уровни
data class DreamLevel(
    val level: Int,
    val title: String,
    val emoji: String,
    val minDreams: Int,
    val maxDreams: Int,
    val color: String // hex
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
    dreamLevels.getOrNull(current.level) // level — 1-based, индекс следующего = level

// Достижения
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: (dreams: List<com.example.journalofdream.model.Dream>, streakDays: Int, categories: Int) -> Boolean
)

val allAchievements = listOf(
    Achievement(
        "first_dream", "Первый шаг", "Запишите первый сон", "🌱",
        { dreams, _, _ -> dreams.isNotEmpty() }
    ),
    Achievement(
        "dreams_5", "Начало пути", "Запишите 5 снов", "📖",
        { dreams, _, _ -> dreams.size >= 5 }
    ),
    Achievement(
        "dreams_10", "Десятка", "Запишите 10 снов", "🔟",
        { dreams, _, _ -> dreams.size >= 10 }
    ),
    Achievement(
        "dreams_25", "Четверть сотни", "Запишите 25 снов", "🌙",
        { dreams, _, _ -> dreams.size >= 25 }
    ),
    Achievement(
        "dreams_50", "Полста снов", "Запишите 50 снов", "⭐",
        { dreams, _, _ -> dreams.size >= 50 }
    ),
    Achievement(
        "dreams_100", "Сотня", "Запишите 100 снов", "💯",
        { dreams, _, _ -> dreams.size >= 100 }
    ),
    Achievement(
        "streak_3", "3 дня подряд", "Записывайте сны 3 дня подряд", "🔥",
        { _, streak, _ -> streak >= 3 }
    ),
    Achievement(
        "streak_7", "Неделя снов", "Записывайте сны 7 дней подряд", "🗓️",
        { _, streak, _ -> streak >= 7 }
    ),
    Achievement(
        "streak_30", "Месяц снов", "Записывайте сны 30 дней подряд", "🏆",
        { _, streak, _ -> streak >= 30 }
    ),
    Achievement(
        "categories_3", "Разнообразие", "Используйте 3 разные категории", "🎨",
        { _, _, cats -> cats >= 3 }
    ),
    Achievement(
        "categories_5", "Коллекционер", "Используйте 5 разных категорий", "🗂️",
        { _, _, cats -> cats >= 5 }
    ),
    Achievement(
        "long_dream", "Подробный сон", "Запишите сон длиннее 500 символов", "📝",
        { dreams, _, _ -> dreams.any { it.content.length >= 500 } }
    ),
    Achievement(
        "night_owl", "Ночная сова", "Запишите 5 снов в один день", "🦉",
        { dreams, _, _ ->
            dreams.groupBy { it.date }.any { it.value.size >= 5 }
        }
    ),
    Achievement(
        "dreamer_week", "Активная неделя", "Запишите сны в 5 из 7 дней недели", "📅",
        { dreams, _, _ ->
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
        "all_levels", "Легенда снов", "Достигните 9-го уровня", "👑",
        { dreams, _, _ -> dreams.size >= 1000 }
    ),

    // --- Новые достижения ---

    // По времени
    Achievement(
        "early_bird", "Ранняя пташка", "Запишите сон до 7 утра", "🌅",
        { dreams, _, _ ->
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
        { dreams, _, _ ->
            dreams.any { dream ->
                try {
                    val hour = dream.time.split(":").firstOrNull()?.toIntOrNull() ?: return@any false
                    hour >= 23 && dream.time.isNotBlank()
                } catch (e: Exception) { false }
            }
        }
    ),
    Achievement(
        "all_weekdays", "Все дни недели", "Запишите хотя бы по одному сну в каждый день недели (пн–вс)", "📅",
        { dreams, _, _ ->
            val weekdays = mutableSetOf<Int>()
            dreams.forEach { dream ->
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val cal = java.util.Calendar.getInstance()
                    cal.time = sdf.parse(dream.date) ?: return@forEach
                    weekdays.add(cal.get(java.util.Calendar.DAY_OF_WEEK))
                } catch (e: Exception) {}
            }
            // DAY_OF_WEEK: 2=пн, 3=вт, 4=ср, 5=чт, 6=пт, 7=сб, 1=вс
            weekdays.containsAll(setOf(1, 2, 3, 4, 5, 6, 7))
        }
    ),

    // По содержанию
    Achievement(
        "nightmare", "Кошмар", "Запишите сон с категорией «Кошмар»", "🔴",
        { dreams, _, _ ->
            dreams.any { it.category.equals("Кошмар", ignoreCase = true) }
        }
    ),
    Achievement(
        "cartographer", "Картограф", "Привяжите локацию к 10 снам", "🗺️",
        { dreams, _, _ ->
            dreams.count { it.locationIds.isNotEmpty() } >= 10
        }
    ),
    Achievement(
        "librarian", "Библиотекарь", "Имейте сны во всех созданных категориях", "📖",
        { dreams, _, cats ->
            cats >= 3 && dreams.map { it.category.ifBlank { "Без категории" } }.toSet().size >= cats
        }
    ),

    // По активности
    Achievement(
        "marathon", "Марафон", "Запишите 3 сна за один день", "⚡",
        { dreams, _, _ ->
            dreams.groupBy { it.date }.any { it.value.size >= 3 }
        }
    ),
    Achievement(
        "comeback", "Возвращение", "Вернитесь после перерыва в 7+ дней и запишите сон", "🔁",
        { dreams, _, _ ->
            if (dreams.size < 2) return@Achievement false
            val sorted = dreams.mapNotNull { dream ->
                try {
                    val fmt = if (dream.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    else
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                    fmt.parse(dream.date)
                } catch (e: Exception) { null }
            }.sorted()
            sorted.zipWithNext().any { (a, b) ->
                val diff = (b.time - a.time) / (1000 * 60 * 60 * 24)
                diff >= 7
            }
        }
    ),
    Achievement(
        "streak_28", "Лунный месяц", "Записывайте сны 28 дней подряд", "🌙",
        { _, streak, _ -> streak >= 28 }
    ),

    // Юмористические
    Achievement(
        "lucky_77", "Везунчик", "Запишите ровно 77 снов", "🎲",
        { dreams, _, _ -> dreams.size == 77 }
    ),
    Achievement(
        "ghost", "Призрак", "Создайте сон без заголовка", "👻",
        { dreams, _, _ ->
            dreams.any { it.title.isBlank() || it.title.trim().isEmpty() }
        }
    ),
    Achievement(
        "secret_agent", "Тайный агент", "Не пишите 30 дней, затем запишите 5 снов подряд", "🤫",
        { dreams, _, _ ->
            if (dreams.size < 5) return@Achievement false
            val sorted = dreams.mapNotNull { dream ->
                try {
                    val fmt = if (dream.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    else
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                    fmt.parse(dream.date)
                } catch (e: Exception) { null }
            }.sorted()
            // Ищем перерыв >= 30 дней, после которого идут 5+ снов
            sorted.zipWithNext().any { (a, b) ->
                val diff = (b.time - a.time) / (1000 * 60 * 60 * 24)
                diff >= 30
            } && sorted.takeLast(5).let { last5 ->
                if (last5.size < 5) false
                else {
                    val span = (last5.last().time - last5.first().time) / (1000 * 60 * 60 * 24)
                    span <= 7
                }
            }
        }
    )
)

fun hexToColorSafe(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toInt(16) / 255f
        val g = clean.substring(2, 4).toInt(16) / 255f
        val b = clean.substring(4, 6).toInt(16) / 255f
        androidx.compose.ui.graphics.Color(r, g, b)
    } catch (e: Exception) { androidx.compose.ui.graphics.Color.Gray }
}
