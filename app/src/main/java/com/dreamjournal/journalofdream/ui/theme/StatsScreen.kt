package com.dreamjournal.journalofdream.ui.theme

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
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

private fun buildTechniqueRecommendations(
    dreams: List<Dream>,
    maxStreak: Int,
    topLocationName: String?,
    topLocationCount: Int,
    topCharacterName: String?,
    topCharacterCount: Int,
    lucidLabels: Set<String>,
    recallTitle: String,
    recallBody: String,
    realityTitle: String,
    realityBody: String,
    locationTitle: (String) -> String,
    locationBody: (String) -> String,
    characterTitle: (String) -> String,
    characterBody: (String) -> String,
    advancedTitle: String,
    advancedBody: String
): List<TechniqueRecommendation> {
    val lucidCount = dreams.count {
        it.category.trim().lowercase(Locale.getDefault()) in lucidLabels
    }

    val recommendations = mutableListOf<TechniqueRecommendation>()

    if (dreams.size < 5) {
        recommendations += TechniqueRecommendation(recallTitle, recallBody)
    }

    if (dreams.size >= 5 && lucidCount == 0) {
        recommendations += TechniqueRecommendation(realityTitle, realityBody)
    }

    if (!topLocationName.isNullOrBlank() && topLocationCount >= 2) {
        recommendations += TechniqueRecommendation(
            locationTitle(topLocationName),
            locationBody(topLocationName)
        )
    }

    if (!topCharacterName.isNullOrBlank() && topCharacterCount >= 2) {
        recommendations += TechniqueRecommendation(
            characterTitle(topCharacterName),
            characterBody(topCharacterName)
        )
    }

    if (lucidCount >= 2 || maxStreak >= 5) {
        recommendations += TechniqueRecommendation(advancedTitle, advancedBody)
    }

    return recommendations.distinctBy { it.title }.take(3)
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
    val locationsWithDreams by locationViewModel.getAllLocationsWithDreams().observeAsState(emptyList())
    val charactersWithDreams by characterViewModel.getAllCharactersWithDreams().observeAsState(emptyList())

    val noCategoryLabel = stringResource(R.string.dreams_no_category)
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val lucidCategoryLabel = stringResource(R.string.cat_lucid)

    val stats = remember(dreams, noCategoryLabel) {
        computeStats(dreams, noCategoryLabel)
    }

    val topLocations = remember(locationsWithDreams) {
        locationsWithDreams.sortedByDescending { it.dreams.size }.take(3)
    }

    val topCharacters = remember(charactersWithDreams) {
        charactersWithDreams.sortedByDescending { it.dreams.size }.take(3)
    }

    // Оставлено для совместимости со старыми данными, где категория могла сохраниться текстом.
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

    val recommendations = remember(
        dreams,
        stats.maxStreak,
        topLocations,
        topCharacters,
        lucidLabels,
        recallTitle,
        recallBody,
        realityTitle,
        realityBody,
        advancedTitle,
        advancedBody
    ) {
        buildTechniqueRecommendations(
            dreams = dreams,
            maxStreak = stats.maxStreak,
            topLocationName = topLocations.firstOrNull()?.location?.name,
            topLocationCount = topLocations.firstOrNull()?.dreams?.size ?: 0,
            topCharacterName = topCharacters.firstOrNull()?.character?.name,
            topCharacterCount = topCharacters.firstOrNull()?.dreams?.size ?: 0,
            lucidLabels = lucidLabels,
            recallTitle = recallTitle,
            recallBody = recallBody,
            realityTitle = realityTitle,
            realityBody = realityBody,
            locationTitle = { name -> "LOC::$name" },
            locationBody = { name -> "LOCBODY::$name" },
            characterTitle = { name -> "CHAR::$name" },
            characterBody = { name -> "CHARBODY::$name" },
            advancedTitle = advancedTitle,
            advancedBody = advancedBody
        )
    }

    val localizedRecommendations = recommendations.mapNotNull { recommendation ->
        when {
            recommendation.title == recallTitle ->
                recommendation

            recommendation.title == realityTitle ->
                recommendation

            recommendation.title == advancedTitle ->
                recommendation

            recommendation.title.startsWith("LOC::") -> {
                val name = recommendation.title.removePrefix("LOC::")
                TechniqueRecommendation(
                    stringResource(R.string.stats_rec_location_title, name),
                    stringResource(R.string.stats_rec_location_body, name)
                )
            }

            recommendation.title.startsWith("CHAR::") -> {
                val name = recommendation.title.removePrefix("CHAR::")
                TechniqueRecommendation(
                    stringResource(R.string.stats_rec_character_title, name),
                    stringResource(R.string.stats_rec_character_body, name)
                )
            }

            else -> null
        }
    }

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
                                label = stringResource(
                                    R.string.stats_best_day,
                                    count,
                                    pluralDreams(count, pluralOne, pluralFew, pluralMany)
                                )
                            )
                        }
                    }

                    if (localizedRecommendations.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_recommendations_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(localizedRecommendations) { recommendation ->
                            RecommendationCard(
                                recommendation = recommendation,
                                onOpenTechniques = { navController.navigate("techniques") }
                            )
                        }
                    }

                    if (topLocations.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_top_locations),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topLocations, key = { it.location.id }) { item ->
                            CategoryStatRow(
                                category = "📍 ${item.location.name}",
                                count = item.dreams.size,
                                total = stats.total
                            )
                        }
                    }

                    if (topCharacters.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_top_characters),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(topCharacters, key = { it.character.id }) { item ->
                            CategoryStatRow(
                                category = "👤 ${item.character.name}",
                                count = item.dreams.size,
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

fun pluralDreams(count: Int, one: String, few: String, many: String): String = when {
    count % 100 in 11..19 -> many
    count % 10 == 1 -> one
    count % 10 in 2..4 -> few
    else -> many
}