package com.example.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.journalofdream.model.*
import com.example.journalofdream.model.hexToColorSafe
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.DreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    locationViewModel: com.example.journalofdream.viewmodel.LocationViewModel? = null
) {
    val dreams by dreamViewModel.dreams.observeAsState(emptyList())
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("achievements_prefs", android.content.Context.MODE_PRIVATE) }

    // Подсчёт стрика
    val streak = remember(dreams) { computeStreak(dreams) }
    val usedCategories = remember(dreams) {
        dreams.map { it.category.ifBlank { context.getString(R.string.dreams_no_category) } }.toSet().size
    }

    // Данные локаций
    val locationsWithDreams by (locationViewModel?.getAllLocationsWithDreams()
        ?: androidx.lifecycle.MutableLiveData(emptyList())).observeAsState(emptyList())

    val locationsCount = locationsWithDreams.size
    // Максимальное количество снов в одной локации
    val maxDreamsInLoc = remember(locationsWithDreams) {
        locationsWithDreams.maxOfOrNull { it.dreams.size } ?: 0
    }
    // Максимальное количество локаций привязанных к одному сну
    val maxLocsInDream = remember(dreams) {
        dreams.maxOfOrNull { it.locationIds.size } ?: 0
    }

    val currentLevel = remember(dreams) { getLevelForCount(dreams.size) }
    val nextLevel = remember(currentLevel) { getNextLevel(currentLevel) }

    // Считаем какие достижения разблокированы сейчас
    val currentlyUnlocked = remember(dreams, streak, usedCategories, locationsCount, maxDreamsInLoc, maxLocsInDream) {
        allAchievements.filter {
            it.isUnlocked(dreams, streak, usedCategories, locationsCount, maxDreamsInLoc, maxLocsInDream)
        }.map { it.id }.toSet()
    }

    // Загружаем ранее сохранённые достижения из SharedPreferences
    val savedUnlocked = remember {
        prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
    }

    // Объединяем — достижения не исчезают после удаления снов
    val unlockedAchievements = remember(currentlyUnlocked, savedUnlocked) {
        (currentlyUnlocked + savedUnlocked).also { merged ->
            // Сохраняем объединённый набор
            prefs.edit().putStringSet("unlocked_achievements", merged).apply()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.achievements_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Карточка уровня
                item {
                    LevelCard(
                        dreams = dreams.size,
                        currentLevel = currentLevel,
                        nextLevel = nextLevel
                    )
                }

                // Заголовок достижений
                item {
                    val unlocked = unlockedAchievements.size
                    val total = allAchievements.size
                    Text(
                        text = stringResource(R.string.achievements_count, unlocked, total),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Сетка достижений 3 в ряд
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 2000.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false
                    ) {
                        items(allAchievements) { achievement ->
                            val isUnlocked = achievement.id in unlockedAchievements
                            AchievementCard(achievement = achievement, isUnlocked = isUnlocked)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun LevelCard(dreams: Int, currentLevel: DreamLevel, nextLevel: DreamLevel?) {
    val context = LocalContext.current
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val levelColor = hexToColorSafe(currentLevel.color)

    // Прогресс до следующего уровня
    val progress = if (nextLevel != null) {
        val range = (nextLevel.minDreams - currentLevel.minDreams).toFloat()
        val done = (dreams - currentLevel.minDreams).toFloat()
        (done / range).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, levelColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Эмодзи уровня с подсветкой
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(levelColor.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(currentLevel.emoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.achievements_level, currentLevel.level),
                color = levelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = getLevelTitle(context, currentLevel.level),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Прогресс-бар
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$dreams ${if (dreams % 100 in 11..19) pluralMany else when (dreams % 10) { 1 -> pluralOne; in 2..4 -> pluralFew; else -> pluralMany }}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (nextLevel != null) stringResource(R.string.achievements_next_level, getLevelTitle(context, nextLevel.level), nextLevel.minDreams - dreams)
                               else stringResource(R.string.achievements_max_level),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(levelColor, levelColor.copy(alpha = 0.6f))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Все уровни в ряд
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dreamLevels.forEach { level ->
                    val isReached = dreams >= level.minDreams
                    val isCurrent = level.level == currentLevel.level
                    val c = hexToColorSafe(level.color)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = level.emoji,
                            fontSize = if (isCurrent) 20.sp else 14.sp,
                            modifier = Modifier.alpha(if (isReached) 1f else 0.3f)
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(c)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement, isUnlocked: Boolean) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            else
                Color.Black.copy(alpha = 0.25f)
        ),
        border = if (isUnlocked) androidx.compose.foundation.BorderStroke(
            1.dp, Color.White.copy(alpha = 0.3f)
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievement.emoji,
                fontSize = 28.sp,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.25f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getAchievementTitle(context, achievement.id),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
            if (isUnlocked) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
            }
        }
    }
}

fun computeStreak(dreams: List<com.example.journalofdream.model.Dream>): Int {
    if (dreams.isEmpty()) return 0
    val dates = dreams.mapNotNull { dream ->
        try {
            val date = dream.date
            if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) date
            else {
                val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                val cal = java.util.Calendar.getInstance()
                cal.time = sdf.parse(date)!!
                String.format("%04d-%02d-%02d",
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH))
            }
        } catch (e: Exception) { null }
    }.toSortedSet(compareByDescending { it })

    val cal = java.util.Calendar.getInstance()
    var streak = 0
    while (true) {
        val key = String.format("%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH))
        if (key in dates) {
            streak++
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
        } else break
    }
    return streak
}
