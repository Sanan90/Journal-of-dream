package com.example.journalofdream.ui.theme

import com.example.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Dream
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

data class StatsData(
    val total: Int,
    val byCategory: Map<String, Int>,
    val maxStreak: Int,
    val bestDay: Pair<String, Int>? // дата -> количество снов
)

fun computeStats(dreams: List<Dream>, noCategoryLabel: String = "Без категории"): StatsData {
    val total = dreams.size

    // По категориям
    val byCategory = dreams
        .groupBy { it.category.ifBlank { noCategoryLabel } }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    // Лучший день
    val byDay = dreams
        .groupBy { it.date }
        .mapValues { it.value.size }
    val bestDay = byDay.maxByOrNull { it.value }?.let { it.key to it.value }

    // Максимальный streak (дней подряд с хотя бы одним сном)
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
        currentStreak = if (prevDay < 0 || day - prevDay == oneDayMs) {
            currentStreak + 1
        } else {
            1
        }
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
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel
) {
    val dreams by dreamViewModel.dreams.observeAsState(emptyList())
    val noCategoryLabel = stringResource(R.string.dreams_no_category)
    val stats = remember(dreams) { computeStats(dreams, noCategoryLabel) }
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                            stringResource(R.string.stats_empty),
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

                    // Главные цифры
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

                    // Лучший день
                    stats.bestDay?.let { (date, count) ->
                        item {
                            StatCard(
                                modifier = Modifier.fillMaxWidth(),
                                emoji = "🏆",
                                value = formatDateForStats(date),
                                label = stringResource(R.string.stats_best_day, count, pluralDreams(count, pluralOne, pluralFew, pluralMany))
                            )
                        }
                    }

                    // По категориям
                    item {
                        Text(
                            text = stringResource(R.string.stats_by_category),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(stats.byCategory.entries.toList()) { (category, count) ->
                        CategoryStatRow(
                            category = localizeCategory(category, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal)),
                            count = count,
                            total = stats.total
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
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
                text = "${(fraction * 100).toInt()}% " + stringResource(R.string.stats_of_all),
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
