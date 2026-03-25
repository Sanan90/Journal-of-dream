package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.Canvas
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark  = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val ChartCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.75f), Color(0xFF1A0C30).copy(alpha = 0.88f))
)

enum class ChartPeriod(val labelRes: Int, val days: Int) {
    WEEK(R.string.chart_7days, 7),
    MONTH(R.string.chart_30days, 30),
    QUARTER(R.string.chart_90days, 90)
}

@Composable
fun ChartScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {
    val dreams by dreamViewModel.dreams.observeAsState(emptyList())
    var period by remember { mutableStateOf(ChartPeriod.MONTH) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Фон ──
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // ── Заголовок ──
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back),
                        tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.chart_title), color = GoldLight, fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold, fontSize = 30.sp, modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.graphic_cosmo_icon), null,
                    Modifier.size(34.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
            }

            // ── Контент ──
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(2.dp))

                // ── Переключатель периода ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChartPeriod.entries.forEach { p ->
                        val isSelected = period == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870)))
                                    else Brush.horizontalGradient(listOf(Color(0xFF2A1545).copy(0.70f), Color(0xFF1A0D30).copy(0.70f)))
                                )
                                .border(1.dp, if (isSelected) GoldLight.copy(0.70f) else Color.White.copy(0.15f), RoundedCornerShape(12.dp))
                                .clickable { period = p }
                                .padding(horizontal = 18.dp, vertical = 9.dp)
                        ) {
                            Text(stringResource(p.labelRes), fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldLight else Color.White.copy(0.65f))
                        }
                    }
                }

                // ── Графики ──
                ChartActivityBarChart(dreams = dreams, period = period)
                ChartSummaryCards(dreams = dreams, period = period)
                ChartWeekdayChart(dreams = dreams)
                ChartHourlyChart(dreams = dreams)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── Карточка-обёртка ─────────────────────────────────────────────────────────
@Composable
private fun ChartCard(title: String, iconRes: Int? = null, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(ChartCardGradient)
            .border(1.dp, GoldLight.copy(0.22f), RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes != null) {
                    Image(painterResource(iconRes), null, Modifier.size(20.dp), contentScale = ContentScale.Fit)
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = PlayfairFamily)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ─── Основной график по дням ───────────────────────────────────────────────────
@Composable
fun ChartActivityBarChart(dreams: List<Dream>, period: ChartPeriod) {
    val data = remember(dreams, period) { buildDailyData(dreams, period.days) }
    val maxVal = remember(data) { data.maxOfOrNull { it.second } ?: 1 }
    val animProgress by animateFloatAsState(1f, tween(800, easing = EaseOutCubic), label = "bar")

    ChartCard(stringResource(R.string.chart_dreams_by_day), R.drawable.graphic_cosmo_icon) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val barCount = data.size; if (barCount == 0) return@Canvas
            val barWidth = (size.width - 8.dp.toPx()) / barCount
            val spacing  = 2.dp.toPx()
            val maxH     = size.height - 20.dp.toPx()
            data.forEachIndexed { i, (_, count) ->
                val bh = if (maxVal > 0) (count.toFloat() / maxVal) * maxH * animProgress else 0f
                val x  = i * barWidth + spacing / 2
                val y  = size.height - bh - 16.dp.toPx()
                if (bh > 0) drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFF0D68C), Color(0xFF9C27B0)), startY = y, endY = size.height - 16.dp.toPx()),
                    topLeft = Offset(x, y), size = Size(barWidth - spacing, bh),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            drawLine(Color.White.copy(0.18f), Offset(0f, size.height - 16.dp.toPx()), Offset(size.width, size.height - 16.dp.toPx()), 1.dp.toPx())
        }
        val labels = buildDateLabels(period.days)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, color = Color.White.copy(0.50f), fontSize = 10.sp) }
        }
    }
}

// ─── Сводка ───────────────────────────────────────────────────────────────────
@Composable
fun ChartSummaryCards(dreams: List<Dream>, period: ChartPeriod) {
    val data       = remember(dreams, period) { buildDailyData(dreams, period.days) }
    val total      = data.sumOf { it.second }
    val activeDays = data.count { it.second > 0 }
    val avg        = if (activeDays > 0) total.toFloat() / activeDays else 0f

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChartMiniCard(R.drawable.dream_journal_icon, stringResource(R.string.chart_total), "$total", Modifier.weight(1f))
        ChartMiniCard(R.drawable.graphic_cosmo_icon, stringResource(R.string.chart_active_days), "$activeDays", Modifier.weight(1f))
        ChartMiniCard(R.drawable.stats_icon, stringResource(R.string.chart_avg), String.format("%.1f", avg), Modifier.weight(1f))
    }
}

@Composable
private fun ChartMiniCard(iconRes: Int, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(ChartCardGradient)
            .border(1.dp, GoldLight.copy(0.22f), RoundedCornerShape(16.dp)).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(iconRes), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.height(4.dp))
            Text(value, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = PlayfairFamily)
            Text(label, color = Color.White.copy(0.62f), fontSize = 10.sp,
                textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}

// ─── По дням недели ───────────────────────────────────────────────────────────
@Composable
fun ChartWeekdayChart(dreams: List<Dream>) {
    val weekdayNames = listOf(
        stringResource(R.string.chart_mon), stringResource(R.string.chart_tue),
        stringResource(R.string.chart_wed), stringResource(R.string.chart_thu),
        stringResource(R.string.chart_fri), stringResource(R.string.chart_sat),
        stringResource(R.string.chart_sun)
    )
    val counts = remember(dreams) {
        val map = IntArray(7)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dreams.forEach { dream ->
            try {
                val cal = Calendar.getInstance(); cal.time = sdf.parse(dream.date)!!
                val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7; map[dow]++
            } catch (_: Exception) {}
        }
        map.toList()
    }
    val maxCount = counts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val animProgress by animateFloatAsState(1f, tween(800, easing = EaseOutCubic), label = "wd")

    ChartCard(stringResource(R.string.chart_by_weekday)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            counts.forEachIndexed { i, count ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    if (count > 0) Text("$count", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    else Text("·", color = Color.White.copy(0.25f), fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.width(28.dp)
                            .height((80 * (count.toFloat() / maxCount) * animProgress).dp.coerceAtLeast(4.dp))
                            .background(
                                Brush.verticalGradient(listOf(GoldLight.copy(0.90f), Color(0xFF7B3FA0))),
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(weekdayNames[i], color = Color.White.copy(0.70f), fontSize = 10.sp)
                }
            }
        }
    }
}

// ─── По часам ─────────────────────────────────────────────────────────────────
@Composable
fun ChartHourlyChart(dreams: List<Dream>) {
    val dreamsWithTime = remember(dreams) { dreams.filter { it.time.isNotBlank() } }
    if (dreamsWithTime.isEmpty()) return

    val hourlyCounts = remember(dreamsWithTime) {
        val map = IntArray(24)
        dreamsWithTime.forEach { dream ->
            val hour = dream.time.split(":").firstOrNull()?.toIntOrNull() ?: return@forEach; map[hour]++
        }
        map.toList()
    }
    val maxCount = hourlyCounts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val animProgress by animateFloatAsState(1f, tween(1000, easing = EaseOutCubic), label = "hr")

    ChartCard(stringResource(R.string.chart_by_time)) {
        Text(stringResource(R.string.chart_time_hint), color = Color.White.copy(0.50f), fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(100.dp)) {
            val bw = size.width / 24f; val mbh = size.height - 20.dp.toPx()
            hourlyCounts.forEachIndexed { hour, count ->
                val bh = (count.toFloat() / maxCount) * mbh * animProgress
                if (bh > 0) {
                    val barColor = when (hour) {
                        in 0..5   -> Color(0xFF3F51B5)
                        in 6..11  -> Color(0xFF9C27B0)
                        in 12..17 -> Color(0xFFF0D68C)
                        else      -> Color(0xFFEF5350)
                    }
                    val x = hour * bw; val y = size.height - bh - 16.dp.toPx()
                    drawRoundRect(color = barColor.copy(0.88f), topLeft = Offset(x + 1.dp.toPx(), y),
                        size = Size(bw - 2.dp.toPx(), bh), cornerRadius = CornerRadius(3.dp.toPx()))
                }
            }
            drawLine(Color.White.copy(0.18f), Offset(0f, size.height - 16.dp.toPx()), Offset(size.width, size.height - 16.dp.toPx()), 1.dp.toPx())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("00:00","06:00","12:00","18:00","23:00").forEach {
                Text(it, color = Color.White.copy(0.42f), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(
                Color(0xFF3F51B5) to stringResource(R.string.chart_night),
                Color(0xFF9C27B0) to stringResource(R.string.chart_morning),
                Color(0xFFF0D68C) to stringResource(R.string.chart_day),
                Color(0xFFEF5350) to stringResource(R.string.chart_evening)
            ).forEach { (color, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(4.dp))
                    Text(label, color = Color.White.copy(0.65f), fontSize = 10.sp)
                }
            }
        }
    }
}

// ─── Вспомогательные ──────────────────────────────────────────────────────────
private fun buildDailyData(dreams: List<Dream>, days: Int): List<Pair<String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dreamMap = dreams.groupBy { it.date }.mapValues { it.value.size }
    return (days - 1 downTo 0).map { offset ->
        val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_MONTH, -offset)
        val key = sdf.format(c.time); key to (dreamMap[key] ?: 0)
    }
}

private fun buildDateLabels(days: Int): List<String> {
    val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val cal = Calendar.getInstance(); val end = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_MONTH, -(days / 2)); val mid = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_MONTH, -(days / 2)); val start = sdf.format(cal.time)
    return listOf(start, mid, end)
}

// Алиасы для совместимости
@Composable fun ActivityBarChart(dreams: List<Dream>, period: ChartPeriod) = ChartActivityBarChart(dreams, period)
@Composable fun SummaryCards(dreams: List<Dream>, period: ChartPeriod) = ChartSummaryCards(dreams, period)
@Composable fun WeekdayChart(dreams: List<Dream>) = ChartWeekdayChart(dreams)
@Composable fun HourlyChart(dreams: List<Dream>) = ChartHourlyChart(dreams)
@Composable fun ChartStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    ChartMiniCard(R.drawable.stats_icon, label, value, modifier)
}
