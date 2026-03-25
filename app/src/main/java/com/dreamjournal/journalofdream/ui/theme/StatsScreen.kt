package com.dreamjournal.journalofdream.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.DreamWithLocations
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark  = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val CardGradient = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.72f), Color(0xFF1A0C30).copy(alpha = 0.82f))
)

// ─── Модели данных ─────────────────────────────────────────────────────────────
data class StatsData(val total: Int, val byCategory: Map<String, Int>, val maxStreak: Int, val bestDay: Pair<String, Int>?)
data class TechniqueRecommendation(val title: String, val body: String)
data class NamedCount(val name: String, val count: Int)
data class PairInsight(val first: String, val second: String, val count: Int)
data class DreamAnalytics(
    val uniqueCharacters: Int, val uniqueLocations: Int,
    val repeatedCharacterCount: Int, val repeatedLocationCount: Int,
    val topCharacters: List<NamedCount>, val topLocations: List<NamedCount>,
    val topCharacterLocationPairs: List<PairInsight>, val topCharacterPairs: List<PairInsight>,
    val strongestCharacter: NamedCount?, val strongestLocation: NamedCount?,
    val strongestCharacterLocationPair: PairInsight?, val strongestCharacterPair: PairInsight?
)

fun computeStats(dreams: List<Dream>, noCategoryLabel: String = "Без категории"): StatsData {
    val total = dreams.size
    val byCategory = dreams.groupBy { it.category.ifBlank { noCategoryLabel } }
        .mapValues { it.value.size }.toList().sortedByDescending { it.second }.toMap()
    val byDay = dreams.groupBy { it.date }.mapValues { it.value.size }
    val bestDay = byDay.maxByOrNull { it.value }?.let { it.key to it.value }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dreamDates = byDay.keys.mapNotNull { runCatching { dateFormat.parse(it) }.getOrNull() }
        .map {
            val cal = Calendar.getInstance(); cal.time = it
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0); cal.timeInMillis
        }.toSortedSet()
    var maxStreak = 0; var currentStreak = 0; var prevDay = -1L
    val oneDayMs = 24 * 60 * 60 * 1000L
    for (day in dreamDates) {
        currentStreak = if (prevDay < 0 || day - prevDay == oneDayMs) currentStreak + 1 else 1
        if (currentStreak > maxStreak) maxStreak = currentStreak; prevDay = day
    }
    return StatsData(total, byCategory, maxStreak, bestDay)
}

fun formatDateForStats(dateStr: String): String = try {
    val i = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val o = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    o.format(i.parse(dateStr)!!)
} catch (_: Exception) { dateStr }

private fun computeDreamAnalytics(dreamsWithDetails: List<DreamWithLocations>): DreamAnalytics {
    val charCounts = linkedMapOf<String, Int>(); val locCounts = linkedMapOf<String, Int>()
    val clCounts = linkedMapOf<Pair<String,String>, Int>(); val ccCounts = linkedMapOf<Pair<String,String>, Int>()
    dreamsWithDetails.forEach { item ->
        val chars = item.characters.map { it.name.trim() }.filter { it.isNotBlank() }.distinct()
        val locs  = item.locations.map  { it.name.trim() }.filter { it.isNotBlank() }.distinct()
        chars.forEach { charCounts[it] = (charCounts[it] ?: 0) + 1 }
        locs.forEach  { locCounts[it]  = (locCounts[it]  ?: 0) + 1 }
        chars.forEach { ch -> locs.forEach { loc -> val k = ch to loc; clCounts[k] = (clCounts[k] ?: 0) + 1 } }
        for (i in chars.indices) for (j in i+1 until chars.size) {
            val ord = listOf(chars[i], chars[j]).sorted(); val k = ord[0] to ord[1]
            ccCounts[k] = (ccCounts[k] ?: 0) + 1
        }
    }
    fun Map<String,Int>.tops() = entries.sortedByDescending { it.value }.take(5).map { NamedCount(it.key, it.value) }
    fun Map<Pair<String,String>,Int>.topP() = entries.sortedByDescending { it.value }.take(5).map { PairInsight(it.key.first, it.key.second, it.value) }
    val tc = charCounts.tops(); val tl = locCounts.tops(); val tcl = clCounts.topP(); val tcc = ccCounts.topP()
    return DreamAnalytics(charCounts.size, locCounts.size, charCounts.count { it.value>=2 }, locCounts.count { it.value>=2 },
        tc, tl, tcl, tcc, tc.firstOrNull(), tl.firstOrNull(), tcl.firstOrNull(), tcc.firstOrNull())
}

private fun safeFormatString(context: Context, @StringRes id: Int, vararg args: Any, fallback: String): String =
    try { context.getString(id, *args) } catch (_: Exception) { fallback }

// ─── Главный экран ─────────────────────────────────────────────────────────────
@Composable
fun StatsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    locationViewModel: LocationViewModel,
    characterViewModel: CharacterViewModel
) {
    val dreams            by dreamViewModel.dreams.observeAsState(emptyList())
    val dreamsWithDetails by dreamViewModel.getDreamsWithDetails().observeAsState(emptyList())
    val noCategoryLabel   = stringResource(R.string.dreams_no_category)
    val pluralOne         = stringResource(R.string.plural_dreams_one)
    val pluralFew         = stringResource(R.string.plural_dreams_few)
    val pluralMany        = stringResource(R.string.plural_dreams_many)
    val lucidLabel        = stringResource(R.string.cat_lucid)
    val context           = LocalContext.current

    val stats     = remember(dreams, noCategoryLabel) { computeStats(dreams, noCategoryLabel) }
    val analytics = remember(dreamsWithDetails) { computeDreamAnalytics(dreamsWithDetails) }
    val lucidLabels = remember(lucidLabel) { setOf("осознанные сны", lucidLabel.trim().lowercase(Locale.getDefault())) }

    val recallTitle   = stringResource(R.string.stats_rec_recall_title)
    val recallBody    = stringResource(R.string.stats_rec_recall_body)
    val realityTitle  = stringResource(R.string.stats_rec_reality_title)
    val realityBody   = stringResource(R.string.stats_rec_reality_body)
    val advancedTitle = stringResource(R.string.stats_rec_advanced_title)
    val advancedBody  = stringResource(R.string.stats_rec_advanced_body)
    val topLocTitle   = stringResource(R.string.stats_top_locations)
    val topCharTitle  = stringResource(R.string.stats_top_characters)
    val clPairsTitle  = stringResource(R.string.stats_character_location_pairs)
    val cPairsTitle   = stringResource(R.string.stats_character_pairs)

    val recurringText = safeFormatString(context, R.string.stats_recurring_overview,
        analytics.repeatedCharacterCount, analytics.repeatedLocationCount, fallback =
        "Повторяющиеся образы: ${analytics.repeatedCharacterCount}. Локации: ${analytics.repeatedLocationCount}.")
    val strongCharText = analytics.strongestCharacter?.let { safeFormatString(context, R.string.stats_hint_top_character, it.name, it.count, fallback = "${it.name} (${it.count})") }
    val strongLocText  = analytics.strongestLocation?.let  { safeFormatString(context, R.string.stats_hint_top_location,  it.name, it.count, fallback = "${it.name} (${it.count})") }
    val strongClText   = analytics.strongestCharacterLocationPair?.let { safeFormatString(context, R.string.stats_hint_top_character_location, it.first, it.second, it.count, fallback = "${it.first} → ${it.second} (${it.count})") }
    val strongCcText   = analytics.strongestCharacterPair?.let { safeFormatString(context, R.string.stats_hint_top_character_pair, it.first, it.second, it.count, fallback = "${it.first} + ${it.second} (${it.count})") }
    val locRecTitle    = analytics.strongestLocation?.name?.let { safeFormatString(context, R.string.stats_rec_location_title, it, fallback = it) }
    val locRecBody     = analytics.strongestLocation?.name?.let { safeFormatString(context, R.string.stats_rec_location_body,  it, fallback = "Эта локация часто повторяется.") }
    val charRecTitle   = analytics.strongestCharacter?.name?.let { safeFormatString(context, R.string.stats_rec_character_title, it, fallback = it) }
    val charRecBody    = analytics.strongestCharacter?.name?.let { safeFormatString(context, R.string.stats_rec_character_body,  it, fallback = "Этот образ часто повторяется.") }

    val recommendations = remember(dreams, stats.maxStreak, analytics.strongestLocation, analytics.strongestCharacter,
        lucidLabels, recallTitle, recallBody, realityTitle, realityBody, advancedTitle, advancedBody, locRecTitle, locRecBody, charRecTitle, charRecBody) {
        val lc = dreams.count { it.category.trim().lowercase(Locale.getDefault()) in lucidLabels }
        buildList {
            if (dreams.size < 5) add(TechniqueRecommendation(recallTitle, recallBody))
            if (dreams.size >= 5 && lc == 0) add(TechniqueRecommendation(realityTitle, realityBody))
            if (!locRecTitle.isNullOrBlank() && !locRecBody.isNullOrBlank() && (analytics.strongestLocation?.count ?: 0) >= 2)
                add(TechniqueRecommendation(locRecTitle, locRecBody))
            if (!charRecTitle.isNullOrBlank() && !charRecBody.isNullOrBlank() && (analytics.strongestCharacter?.count ?: 0) >= 2)
                add(TechniqueRecommendation(charRecTitle, charRecBody))
            if (lc >= 2 || stats.maxStreak >= 5) add(TechniqueRecommendation(advancedTitle, advancedBody))
        }.distinctBy { it.title }.take(3)
    }

    val topLocations  = analytics.topLocations.take(5)
    val topCharacters = analytics.topCharacters.take(5)
    val topClPairs    = analytics.topCharacterLocationPairs.take(5)
    val topCcPairs    = analytics.topCharacterPairs.take(5)

    // ── Фон + Layout ──────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // Заголовок
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back), tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.stats_title), color = GoldLight, fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold, fontSize = 30.sp, modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.stats_icon), null,
                    Modifier.size(34.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
            }

            if (dreams.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(R.drawable.stats_icon), null, Modifier.size(80.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.stats_empty), color = Color.White.copy(0.7f), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)) {

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatsStatCard(Modifier.weight(1f), "🌙", stats.total.toString(), stringResource(R.string.stats_total))
                            StatsStatCard(Modifier.weight(1f), "🔥", stats.maxStreak.toString(), stringResource(R.string.stats_streak))
                        }
                    }
                    stats.bestDay?.let { (date, count) ->
                        item { StatsStatCard(Modifier.fillMaxWidth(), "🏆", formatDateForStats(date), "$count ${pluralDreams(count, pluralOne, pluralFew, pluralMany)}") }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatsStatCard(Modifier.weight(1f), "👤", analytics.uniqueCharacters.toString(), stringResource(R.string.stats_unique_characters))
                            StatsStatCard(Modifier.weight(1f), "📍", analytics.uniqueLocations.toString(), stringResource(R.string.stats_unique_locations))
                        }
                    }
                    item { StatsInsightCard(stringResource(R.string.stats_recurring_summary), recurringText) }
                    if (analytics.strongestCharacter != null || analytics.strongestLocation != null ||
                        analytics.strongestCharacterLocationPair != null || analytics.strongestCharacterPair != null)
                        item { StatsSectionHeader(stringResource(R.string.stats_deeper_insights_title)) }
                    strongCharText?.let { item { StatsInsightCard(topCharTitle, it) } }
                    strongLocText?.let  { item { StatsInsightCard(topLocTitle,  it) } }
                    strongClText?.let   { item { StatsInsightCard(clPairsTitle, it) } }
                    strongCcText?.let   { item { StatsInsightCard(cPairsTitle,  it) } }
                    if (recommendations.isNotEmpty()) {
                        item { StatsSectionHeader(stringResource(R.string.stats_recommendations_title)) }
                        items(recommendations) { rec -> StatsRecommendationCard(rec) { navController.navigate("techniques") } }
                    }
                    if (topCharacters.isNotEmpty()) {
                        item { StatsSectionHeader(topCharTitle) }
                        items(topCharacters) { StatsCategoryRow("👤 ${it.name}", it.count, stats.total) }
                    }
                    if (topLocations.isNotEmpty()) {
                        item { StatsSectionHeader(topLocTitle) }
                        items(topLocations) { StatsCategoryRow("📍 ${it.name}", it.count, stats.total) }
                    }
                    if (topClPairs.isNotEmpty()) {
                        item { StatsSectionHeader(clPairsTitle) }
                        items(topClPairs) { StatsCategoryRow("👤 ${it.first} • 📍 ${it.second}", it.count, stats.total) }
                    }
                    if (topCcPairs.isNotEmpty()) {
                        item { StatsSectionHeader(cPairsTitle) }
                        items(topCcPairs) { StatsCategoryRow("👤 ${it.first} + ${it.second}", it.count, stats.total) }
                    }
                    item { StatsSectionHeader(stringResource(R.string.stats_by_category)) }
                    items(stats.byCategory.toList()) { (cat, cnt) -> StatsCategoryRow(cat, cnt, stats.total) }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ─── Компоненты ───────────────────────────────────────────────────────────────

@Composable
fun StatsSectionHeader(title: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(22.dp)
            .background(Brush.verticalGradient(listOf(GoldLight, GoldDark)), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Text(title, color = GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun StatsStatCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(CardGradient)
            .border(1.dp, GoldLight.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(vertical = 18.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GoldLight, fontFamily = PlayfairFamily)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color.White.copy(0.70f), textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun StatsInsightCard(title: String, body: String) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardGradient)
            .border(1.dp, GoldLight.copy(alpha = 0.22f), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(GoldLight, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldLight)
            }
            Spacer(Modifier.height(8.dp))
            Text(body, fontSize = 13.sp, color = Color.White.copy(0.88f), lineHeight = 18.sp)
        }
    }
}

@Composable
fun StatsRecommendationCard(recommendation: TechniqueRecommendation, onOpenTechniques: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF4A2070).copy(0.75f), Color(0xFF1E0D40).copy(0.85f))))
            .border(1.dp, GoldLight.copy(0.35f), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = GoldLight, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(recommendation.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldLight)
            }
            Spacer(Modifier.height(8.dp))
            Text(recommendation.body, fontSize = 13.sp, color = Color.White.copy(0.85f), lineHeight = 18.sp)
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(GoldLight.copy(0.15f)).border(1.dp, GoldLight.copy(0.38f), RoundedCornerShape(10.dp))
                    .clickable { onOpenTechniques() }.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.magic_book_icon), null, Modifier.size(16.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.stats_open_techniques), color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun StatsCategoryRow(category: String, count: Int, total: Int) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    val pluralOne  = stringResource(R.string.plural_dreams_one)
    val pluralFew  = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardGradient)
            .border(1.dp, Color.White.copy(0.09f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("$count ${pluralDreams(count, pluralOne, pluralFew, pluralMany)}", color = GoldLight, fontSize = 13.sp)
            }
            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(0.10f))) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(GoldDark, GoldLight)), RoundedCornerShape(3.dp)))
            }
            Text("${(fraction * 100).toInt()}% ${stringResource(R.string.stats_of_all)}",
                fontSize = 10.sp, color = Color.White.copy(0.42f), modifier = Modifier.padding(top = 3.dp))
        }
    }
}

// ─── Алиасы для совместимости с другими файлами ────────────────────────────────
@Composable fun InsightCard(title: String, body: String) = StatsInsightCard(title, body)
@Composable fun RecommendationCard(r: TechniqueRecommendation, onClick: () -> Unit) = StatsRecommendationCard(r, onClick)
@Composable fun StatCard(modifier: Modifier = Modifier, emoji: String, value: String, label: String) = StatsStatCard(modifier, emoji, value, label)
@Composable fun CategoryStatRow(category: String, count: Int, total: Int) = StatsCategoryRow(category, count, total)
@Composable fun PairStatRow(title: String, count: Int, total: Int) = StatsCategoryRow(title, count, total)

fun pluralDreams(count: Int, one: String, few: String, many: String): String = when {
    count % 100 in 11..19 -> many
    count % 10 == 1        -> one
    count % 10 in 2..4     -> few
    else                   -> many
}
