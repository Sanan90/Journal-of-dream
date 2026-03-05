package com.example.journalofdream.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Dream
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

// Периоды просмотра
enum class ChartPeriod(val label: String, val days: Int) {
    WEEK("7 дней", 7),
    MONTH("30 дней", 30),
    QUARTER("90 дней", 90)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel
) {
    val dreams by dreamViewModel.dreams.observeAsState(emptyList())
    var period by remember { mutableStateOf(ChartPeriod.MONTH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("График активности", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Переключатель периода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChartPeriod.entries.forEach { p ->
                        FilterChip(
                            selected = period == p,
                            onClick = { period = p },
                            label = { Text(p.label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7E57C2),
                                selectedLabelColor = Color.White,
                                containerColor = Color.Black.copy(alpha = 0.3f),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                // Основной график
                ActivityBarChart(dreams = dreams, period = period)

                // Сводка
                SummaryCards(dreams = dreams, period = period)

                // Лучшие дни недели
                WeekdayChart(dreams = dreams)

                // Активность по часам
                HourlyChart(dreams = dreams)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActivityBarChart(dreams: List<Dream>, period: ChartPeriod) {
    val data = remember(dreams, period) { buildDailyData(dreams, period.days) }
    val maxVal = remember(data) { data.maxOfOrNull { it.second } ?: 1 }

    // Анимация столбиков
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_anim"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Сны по дням",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val barCount = data.size
                if (barCount == 0) return@Canvas
                val barWidth = (size.width - 8.dp.toPx()) / barCount
                val spacing = 2.dp.toPx()
                val maxHeight = size.height - 20.dp.toPx()

                data.forEachIndexed { index, (_, count) ->
                    val barHeight = if (maxVal > 0)
                        (count.toFloat() / maxVal) * maxHeight * animProgress
                    else 0f

                    val x = index * barWidth + spacing / 2
                    val y = size.height - barHeight - 16.dp.toPx()

                    if (barHeight > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF9C27B0), Color(0xFF3F51B5)),
                                startY = y,
                                endY = size.height - 16.dp.toPx()
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth - spacing, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }

                // Базовая линия
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, size.height - 16.dp.toPx()),
                    end = Offset(size.width, size.height - 16.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Метки дат (начало, середина, конец)
            val labels = buildDateLabels(period.days)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryCards(dreams: List<Dream>, period: ChartPeriod) {
    val data = remember(dreams, period) { buildDailyData(dreams, period.days) }
    val total = data.sumOf { it.second }
    val activeDays = data.count { it.second > 0 }
    val maxDay = data.maxByOrNull { it.second }
    val avg = if (activeDays > 0) total.toFloat() / activeDays else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ChartStatCard("📝", "Всего снов", "$total", Modifier.weight(1f))
        ChartStatCard("📅", "Активных дней", "$activeDays", Modifier.weight(1f))
        ChartStatCard("📈", "Среднее/день", String.format("%.1f", avg), Modifier.weight(1f))
    }
}

@Composable
fun ChartStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp)
        }
    }
}

@Composable
fun WeekdayChart(dreams: List<Dream>) {
    // Счёт по дням недели
    val weekdayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val counts = remember(dreams) {
        val map = IntArray(7)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dreams.forEach { dream ->
            try {
                val cal = Calendar.getInstance()
                cal.time = sdf.parse(dream.date)!!
                // Calendar.DAY_OF_WEEK: 1=вс, 2=пн..7=сб → переводим в 0=пн..6=вс
                val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                map[dow]++
            } catch (e: Exception) {}
        }
        map.toList()
    }
    val maxCount = counts.maxOrNull()?.takeIf { it > 0 } ?: 1

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "weekday_anim"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("По дням недели", color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                counts.forEachIndexed { i, count ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "$count",
                            color = if (count > 0) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height((80 * (count.toFloat() / maxCount) * animProgress).dp.coerceAtLeast(4.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF7E57C2), Color(0xFF3F51B5))
                                    ),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(weekdayNames[i], color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyChart(dreams: List<Dream>) {
    val dreamsWithTime = remember(dreams) { dreams.filter { it.time.isNotBlank() } }
    if (dreamsWithTime.isEmpty()) return

    // Группируем по часу
    val hourlyCounts = remember(dreamsWithTime) {
        val map = IntArray(24)
        dreamsWithTime.forEach { dream ->
            val hour = dream.time.split(":").firstOrNull()?.toIntOrNull() ?: return@forEach
            map[hour]++
        }
        map.toList()
    }
    val maxCount = hourlyCounts.maxOrNull()?.takeIf { it > 0 } ?: 1

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "hour_anim"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("По времени суток", color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Когда чаще всего записываешь сны",
                color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val barWidth = size.width / 24f
                val maxBarHeight = size.height - 20.dp.toPx()

                hourlyCounts.forEachIndexed { hour, count ->
                    val barHeight = (count.toFloat() / maxCount) * maxBarHeight * animProgress
                    if (barHeight > 0) {
                        // Ночь — синий, утро — фиолетовый, день — оранжевый, вечер — красный
                        val barColor = when (hour) {
                            in 0..5 -> Color(0xFF1A237E)
                            in 6..11 -> Color(0xFF7E57C2)
                            in 12..17 -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        }
                        val x = hour * barWidth
                        val y = size.height - barHeight - 16.dp.toPx()
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.85f),
                            topLeft = Offset(x + 1.dp.toPx(), y),
                            size = Size(barWidth - 2.dp.toPx(), barHeight),
                            cornerRadius = CornerRadius(3.dp.toPx())
                        )
                    }
                }
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, size.height - 16.dp.toPx()),
                    end = Offset(size.width, size.height - 16.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Метки времени
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00:00", "06:00", "12:00", "18:00", "23:00").forEach { t ->
                    Text(t, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Легенда
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Color(0xFF1A237E) to "Ночь",
                    Color(0xFF7E57C2) to "Утро",
                    Color(0xFFFFA726) to "День",
                    Color(0xFFEF5350) to "Вечер"
                ).forEach { (color, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Строит список (дата, количество снов) за последние N дней
private fun buildDailyData(dreams: List<Dream>, days: Int): List<Pair<String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val dreamMap = dreams.groupBy { it.date }.mapValues { it.value.size }
    return (days - 1 downTo 0).map { offset ->
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, -offset)
        val key = sdf.format(c.time)
        key to (dreamMap[key] ?: 0)
    }
}

private fun buildDateLabels(days: Int): List<String> {
    val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val end = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_MONTH, -(days / 2))
    val mid = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_MONTH, -(days / 2))
    val start = sdf.format(cal.time)
    return listOf(start, mid, end)
}
