package com.dreamjournal.journalofdream.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.DreamWithLocations
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import androidx.compose.ui.platform.LocalContext

data class StatsData(
    val total: Int,
    val byCategory: Map<String, Int>,
    val maxStreak: Int,
    val bestDay: Pair<String, Int>?
)

data class TechniqueRecommendation(
    val title: String,
    val body: String
)

data class NamedCount(
    val name: String,
    val count: Int
)

data class PairInsight(
    val first: String,
    val second: String,
    val count: Int
)

data class DreamAnalytics(
    val uniqueCharacters: Int,
    val uniqueLocations: Int,
    val repeatedCharacterCount: Int,
    val repeatedLocationCount: Int,
    val topCharacters: List<NamedCount>,
    val topLocations: List<NamedCount>,
    val topCharacterLocationPairs: List<PairInsight>,
    val topCharacterPairs: List<PairInsight>,
    val strongestCharacter: NamedCount?,
    val strongestLocation: NamedCount?,
    val strongestCharacterLocationPair: PairInsight?,
    val strongestCharacterPair: PairInsight?
)

fun computeStats(dreams: List<Dream>, noCategoryLabel: String = "Без категории"): StatsData {
    val total = dreams.size
    val byCategory = dreams
        .groupBy { it.category.ifBlank { noCategoryLabel } }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    val byDay = dreams.groupBy { it.date }.mapValues { it.value.size }
    val bestDay = byDay.maxByOrNull { it.value }?.let { it.key to it.value }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dreamDates = byDay.keys
        .mapNotNull { runCatching { dateFormat.parse(it) }.getOrNull() }
        .map {
            val cal = Calendar.getInstance()
            cal.time = it
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        .toSortedSet()

    var maxStreak = 0
    var currentStreak = 0
    var prevDay = -1L
    val oneDayMs = 24 * 60 * 60 * 1000L

    for (day in dreamDates) {
        currentStreak = if (prevDay < 0 || day - prevDay == oneDayMs) currentStreak + 1 else 1
        if (currentStreak > maxStreak) maxStreak = currentStreak
        prevDay = day
    }

    return StatsData(total, byCategory, maxStreak, bestDay)
}

fun formatDateForStats(dateStr: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(dateStr)!!)
    } catch (_: Exception) {
        dateStr
    }
}

private fun computeDreamAnalytics(dreamsWithDetails: List<DreamWithLocations>): DreamAnalytics {
    val characterCounts = linkedMapOf<String, Int>()
    val locationCounts = linkedMapOf<String, Int>()
    val characterLocationCounts = linkedMapOf<Pair<String, String>, Int>()
    val characterPairCounts = linkedMapOf<Pair<String, String>, Int>()

    dreamsWithDetails.forEach { item ->
        val characters = item.characters.map { it.name.trim() }.filter { it.isNotBlank() }.distinct()
        val locations = item.locations.map { it.name.trim() }.filter { it.isNotBlank() }.distinct()

        characters.forEach { name ->
            characterCounts[name] = (characterCounts[name] ?: 0) + 1
        }
        locations.forEach { name ->
            locationCounts[name] = (locationCounts[name] ?: 0) + 1
        }

        characters.forEach { character ->
            locations.forEach { location ->
                val key = character to location
                characterLocationCounts[key] = (characterLocationCounts[key] ?: 0) + 1
            }
        }

        for (i in characters.indices) {
            for (j in i + 1 until characters.size) {
                val ordered = listOf(characters[i], characters[j]).sorted()
                val key = ordered[0] to ordered[1]
                characterPairCounts[key] = (characterPairCounts[key] ?: 0) + 1
            }
        }
    }

    fun Map<String, Int>.toTopCounts(): List<NamedCount> =
        entries.sortedByDescending { it.value }.take(5).map { NamedCount(it.key, it.value) }

    fun Map<Pair<String, String>, Int>.toTopPairs(): List<PairInsight> =
        entries.sortedByDescending { it.value }.take(5).map { PairInsight(it.key.first, it.key.second, it.value) }

    val topCharacters = characterCounts.toTopCounts()
    val topLocations = locationCounts.toTopCounts()
    val topCharacterLocationPairs = characterLocationCounts.toTopPairs()
    val topCharacterPairs = characterPairCounts.toTopPairs()

    return DreamAnalytics(
        uniqueCharacters = characterCounts.size,
        uniqueLocations = locationCounts.size,
        repeatedCharacterCount = characterCounts.count { it.value >= 2 },
        repeatedLocationCount = locationCounts.count { it.value >= 2 },
        topCharacters = topCharacters,
        topLocations = topLocations,
        topCharacterLocationPairs = topCharacterLocationPairs,
        topCharacterPairs = topCharacterPairs,
        strongestCharacter = topCharacters.firstOrNull(),
        strongestLocation = topLocations.firstOrNull(),
        strongestCharacterLocationPair = topCharacterLocationPairs.firstOrNull(),
        strongestCharacterPair = topCharacterPairs.firstOrNull()
    )
}

private fun safeFormatString(
    context: Context,
    @StringRes id: Int,
    vararg formatArgs: Any,
    fallback: String
): String {
    return try {
        context.getString(id, *formatArgs)
    } catch (_: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    locationViewModel: LocationViewModel,
    characterViewModel: CharacterViewModel
) {
    val dreams by dreamViewModel.dreams.observeAsState(emptyList())
    val dreamsWithDetails by dreamViewModel.getDreamsWithDetails().observeAsState(emptyList())
    val noCategoryLabel = stringResource(R.string.dreams_no_category)
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val lucidCategoryLabel = stringResource(R.string.cat_lucid)
    val context = LocalContext.current

    val stats = remember(dreams, noCategoryLabel) {
        computeStats(dreams, noCategoryLabel)
    }

    val analytics = remember(dreamsWithDetails) {
        computeDreamAnalytics(dreamsWithDetails)
    }

    // Для совместимости со старыми данными, где категория могла быть сохранена текстом.
    val lucidLabels = remember(lucidCategoryLabel) {
        setOf(
            "осознанные сны",
            lucidCategoryLabel.trim().lowercase(Locale.getDefault())
        )
    }

    val recallTitle = stringResource(R.string.stats_rec_recall_title)
    val recallBody = stringResource(R.string.stats_rec_recall_body)
    val realityTitle = stringResource(R.string.stats_rec_reality_title)
    val realityBody = stringResource(R.string.stats_rec_reality_body)
    val advancedTitle = stringResource(R.string.stats_rec_advanced_title)
    val advancedBody = stringResource(R.string.stats_rec_advanced_body)

    val topLocationsTitle = stringResource(R.string.stats_top_locations)
    val topCharactersTitle = stringResource(R.string.stats_top_characters)
    val charLocPairsTitle = stringResource(R.string.stats_character_location_pairs)
    val charPairsTitle = stringResource(R.string.stats_character_pairs)


    val recurringOverviewText = safeFormatString(
        context,
        R.string.stats_recurring_overview,
        analytics.repeatedCharacterCount,
        analytics.repeatedLocationCount,
        fallback = "Повторяющиеся образы: ${analytics.repeatedCharacterCount}. Повторяющиеся локации: ${analytics.repeatedLocationCount}."
    )

    val strongestCharacterText = analytics.strongestCharacter?.let {
        safeFormatString(
            context,
            R.string.stats_hint_top_character,
            it.name,
            it.count,
            fallback = "Чаще всего повторяется образ: ${it.name} (${it.count})."
        )
    }

    val strongestLocationText = analytics.strongestLocation?.let {
        safeFormatString(
            context,
            R.string.stats_hint_top_location,
            it.name,
            it.count,
            fallback = "Чаще всего повторяется локация: ${it.name} (${it.count})."
        )
    }

    val strongestCharacterLocationText = analytics.strongestCharacterLocationPair?.let {
        safeFormatString(
            context,
            R.string.stats_hint_top_character_location,
            it.first,
            it.second,
            it.count,
            fallback = "Самая частая связка: ${it.first} → ${it.second} (${it.count})."
        )
    }

    val strongestCharacterPairText = analytics.strongestCharacterPair?.let {
        safeFormatString(
            context,
            R.string.stats_hint_top_character_pair,
            it.first,
            it.second,
            it.count,
            fallback = "Самая частая пара образов: ${it.first} + ${it.second} (${it.count})."
        )
    }

    val locationRecTitle = analytics.strongestLocation?.name?.let {
        safeFormatString(
            context,
            R.string.stats_rec_location_title,
            it,
            fallback = "$topLocationsTitle: $it"
        )
    }
    val locationRecBody = analytics.strongestLocation?.name?.let {
        safeFormatString(
            context,
            R.string.stats_rec_location_body,
            it,
            fallback = "Эта локация часто повторяется. Используй её как триггер для проверки реальности."
        )
    }

    val characterRecTitle = analytics.strongestCharacter?.name?.let {
        safeFormatString(
            context,
            R.string.stats_rec_character_title,
            it,
            fallback = "$topCharactersTitle: $it"
        )
    }
    val characterRecBody = analytics.strongestCharacter?.name?.let {
        safeFormatString(
            context,
            R.string.stats_rec_character_body,
            it,
            fallback = "Этот образ часто повторяется. Попробуй распознавать его как знак сна."
        )
    }

    val recommendations = remember(
        dreams,
        stats.maxStreak,
        analytics.strongestLocation,
        analytics.strongestCharacter,
        lucidLabels,
        recallTitle,
        recallBody,
        realityTitle,
        realityBody,
        advancedTitle,
        advancedBody,
        locationRecTitle,
        locationRecBody,
        characterRecTitle,
        characterRecBody
    ) {
        val lucidCount = dreams.count {
            it.category.trim().lowercase(Locale.getDefault()) in lucidLabels
        }

        buildList {
            if (dreams.size < 5) {
                add(TechniqueRecommendation(recallTitle, recallBody))
            }
            if (dreams.size >= 5 && lucidCount == 0) {
                add(TechniqueRecommendation(realityTitle, realityBody))
            }
            if (!locationRecTitle.isNullOrBlank() && !locationRecBody.isNullOrBlank() && (analytics.strongestLocation?.count ?: 0) >= 2) {
                add(TechniqueRecommendation(locationRecTitle, locationRecBody))
            }
            if (!characterRecTitle.isNullOrBlank() && !characterRecBody.isNullOrBlank() && (analytics.strongestCharacter?.count ?: 0) >= 2) {
                add(TechniqueRecommendation(characterRecTitle, characterRecBody))
            }
            if (lucidCount >= 2 || stats.maxStreak >= 5) {
                add(TechniqueRecommendation(advancedTitle, advancedBody))
            }
        }.distinctBy { it.title }.take(3)
    }

    val topLocations = remember(analytics.topLocations) { analytics.topLocations.take(5) }
    val topCharacters = remember(analytics.topCharacters) { analytics.topCharacters.take(5) }
    val topCharacterLocationPairs = remember(analytics.topCharacterLocationPairs) { analytics.topCharacterLocationPairs.take(5) }
    val topCharacterPairs = remember(analytics.topCharacterPairs) { analytics.topCharacterPairs.take(5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            if (dreams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.stats_empty),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "🌙",
                                value = stats.total.toString(),
                                label = stringResource(R.string.stats_total)
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "🔥",
                                value = stats.maxStreak.toString(),
                                label = stringResource(R.string.stats_streak)
                            )
                        }
                    }

                    stats.bestDay?.let { (date, count) ->
                        item {
                            StatCard(
                                modifier = Modifier.fillMaxWidth(),
                                emoji = "🏆",
                                value = formatDateForStats(date),
                                label = "$count ${pluralDreams(count, pluralOne, pluralFew, pluralMany)}"
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "👤",
                                value = analytics.uniqueCharacters.toString(),
                                label = stringResource(R.string.stats_unique_characters)
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "📍",
                                value = analytics.uniqueLocations.toString(),
                                label = stringResource(R.string.stats_unique_locations)
                            )
                        }
                    }

                    item {
                        InsightCard(
                            title = stringResource(R.string.stats_recurring_summary),
                            body = recurringOverviewText
                        )
                    }

                    if (
                        analytics.strongestCharacter != null ||
                        analytics.strongestLocation != null ||
                        analytics.strongestCharacterLocationPair != null ||
                        analytics.strongestCharacterPair != null
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_deeper_insights_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    strongestCharacterText?.let {
                        item {
                            InsightCard(
                                title = topCharactersTitle,
                                body = it
                            )
                        }
                    }

                    strongestLocationText?.let {
                        item {
                            InsightCard(
                                title = topLocationsTitle,
                                body = it
                            )
                        }
                    }

                    strongestCharacterLocationText?.let {
                        item {
                            InsightCard(
                                title = charLocPairsTitle,
                                body = it
                            )
                        }
                    }

                    strongestCharacterPairText?.let {
                        item {
                            InsightCard(
                                title = charPairsTitle,
                                body = it
                            )
                        }
                    }

                    if (recommendations.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_recommendations_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(recommendations) { recommendation ->
                            RecommendationCard(
                                recommendation = recommendation,
                                onOpenTechniques = { navController.navigate("techniques") }
                            )
                        }
                    }

                    if (topCharacters.isNotEmpty()) {
                        item {
                            Text(
                                text = topCharactersTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topCharacters) { item ->
                            CategoryStatRow(
                                category = "👤 ${item.name}",
                                count = item.count,
                                total = stats.total
                            )
                        }
                    }

                    if (topLocations.isNotEmpty()) {
                        item {
                            Text(
                                text = topLocationsTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topLocations) { item ->
                            CategoryStatRow(
                                category = "📍 ${item.name}",
                                count = item.count,
                                total = stats.total
                            )
                        }
                    }

                    if (topCharacterLocationPairs.isNotEmpty()) {
                        item {
                            Text(
                                text = charLocPairsTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topCharacterLocationPairs) { item ->
                            PairStatRow(
                                title = "👤 ${item.first} • 📍 ${item.second}",
                                count = item.count,
                                total = stats.total
                            )
                        }
                    }

                    if (topCharacterPairs.isNotEmpty()) {
                        item {
                            Text(
                                text = charPairsTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topCharacterPairs) { item ->
                            PairStatRow(
                                title = "👤 ${item.first} + ${item.second}",
                                count = item.count,
                                total = stats.total
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.stats_by_category),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(stats.byCategory.toList()) { (category, count) ->
                        CategoryStatRow(
                            category = category,
                            count = count,
                            total = stats.total
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun InsightCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: TechniqueRecommendation,
    onOpenTechniques: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recommendation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommendation.body,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onOpenTechniques) {
                Icon(Icons.Default.MenuBook, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.stats_open_techniques))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CategoryStatRow(category: String, count: Int, total: Int) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count ${pluralDreams(count, pluralOne, pluralFew, pluralMany)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Text(
                text = "${(fraction * 100).toInt()}% ${stringResource(R.string.stats_of_all)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PairStatRow(title: String, count: Int, total: Int) {
    CategoryStatRow(category = title, count = count, total = total)
}

fun pluralDreams(count: Int, one: String, few: String, many: String): String = when {
    count % 100 in 11..19 -> many
    count % 10 == 1 -> one
    count % 10 in 2..4 -> few
    else -> many
}