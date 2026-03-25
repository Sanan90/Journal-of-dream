package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.*
import com.dreamjournal.journalofdream.model.hexToColorSafe
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel

private val GoldLight     = Color(0xFFF0D68C)
private val GoldDark      = Color(0xFFD4A76A)
private val GoldGlow      = Color(0xFFFFE680)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

private val CardGradient = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.78f), Color(0xFF1A0C30).copy(alpha = 0.88f))
)
private val CardGradientUnlocked = Brush.verticalGradient(
    listOf(Color(0xFF4A2278).copy(alpha = 0.85f), Color(0xFF1E0D40).copy(alpha = 0.92f))
)

@Composable
fun AchievementsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    locationViewModel: com.dreamjournal.journalofdream.viewmodel.LocationViewModel? = null
) {
    val dreams     by dreamViewModel.dreams.observeAsState(emptyList())
    val pluralOne  = stringResource(R.string.plural_dreams_one)
    val pluralFew  = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val context    = LocalContext.current
    val prefs      = remember { context.getSharedPreferences("achievements_prefs", android.content.Context.MODE_PRIVATE) }

    val streak         = remember(dreams) { computeStreak(dreams) }
    val usedCategories = remember(dreams) { dreams.map { it.category.ifBlank { context.getString(R.string.dreams_no_category) } }.toSet().size }

    val locationsWithDreams by (locationViewModel?.getAllLocationsWithDreams()
        ?: androidx.lifecycle.MutableLiveData(emptyList())).observeAsState(emptyList())

    val locationsCount  = locationsWithDreams.size
    val maxDreamsInLoc  = remember(locationsWithDreams) { locationsWithDreams.maxOfOrNull { it.dreams.size } ?: 0 }
    val maxLocsInDream  = remember(dreams) { dreams.maxOfOrNull { it.locationIds.size } ?: 0 }

    val currentLevel = remember(dreams) { getLevelForCount(dreams.size) }
    val nextLevel    = remember(currentLevel) { getNextLevel(currentLevel) }

    val currentlyUnlocked = remember(dreams, streak, usedCategories, locationsCount, maxDreamsInLoc, maxLocsInDream) {
        allAchievements.filter {
            it.isUnlocked(dreams, streak, usedCategories, locationsCount, maxDreamsInLoc, maxLocsInDream)
        }.map { it.id }.toSet()
    }

    val savedUnlocked = remember { prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet() }

    val unlockedAchievements = remember(currentlyUnlocked, savedUnlocked) {
        (currentlyUnlocked + savedUnlocked).also { merged ->
            prefs.edit().putStringSet("unlocked_achievements", merged).apply()
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        // Фон
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Заголовок ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back),
                        tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = stringResource(R.string.achievements_title),
                    color = GoldLight,
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.weight(1f)
                )
                Text("🏆", fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
            }

            // ── Список ──
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
            ) {
                // Карточка уровня
                item {
                    AchievementsLevelCard(
                        dreams = dreams.size,
                        currentLevel = currentLevel,
                        nextLevel = nextLevel,
                        pluralOne = pluralOne,
                        pluralFew = pluralFew,
                        pluralMany = pluralMany
                    )
                }

                // Заголовок сетки
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp).height(22.dp)
                                .background(Brush.verticalGradient(listOf(GoldLight, GoldDark)), RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.achievements_count, unlockedAchievements.size, allAchievements.size),
                            color = GoldLight,
                            fontFamily = PlayfairFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Сетка достижений
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(allAchievements) { achievement ->
                            AchievementsCard(
                                achievement = achievement,
                                isUnlocked = achievement.id in unlockedAchievements
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── Карточка уровня ──────────────────────────────────────────────────────────
@Composable
fun AchievementsLevelCard(
    dreams: Int,
    currentLevel: DreamLevel,
    nextLevel: DreamLevel?,
    pluralOne: String,
    pluralFew: String,
    pluralMany: String
) {
    val context    = LocalContext.current
    val levelColor = hexToColorSafe(currentLevel.color)

    val progress = if (nextLevel != null) {
        val range = (nextLevel.minDreams - currentLevel.minDreams).toFloat()
        val done  = (dreams - currentLevel.minDreams).toFloat()
        (done / range).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "level_progress"
    )

    // Пульсация свечения эмодзи
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF4A2278).copy(alpha = 0.85f),
                        Color(0xFF1E0840).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.verticalGradient(listOf(GoldLight.copy(0.60f), GoldDark.copy(0.25f))),
                RoundedCornerShape(26.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Эмодзи с пульсирующим свечением
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Внешнее свечение
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(levelColor.copy(alpha = glowAlpha), Color.Transparent)
                            )
                        )
                )
                // Внутреннее кольцо
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(levelColor.copy(0.30f), Color(0xFF1A0840).copy(0.60f))
                            )
                        )
                        .border(1.dp, levelColor.copy(0.50f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentLevel.emoji, fontSize = 36.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Уровень + название
            Text(
                text = stringResource(R.string.achievements_level, currentLevel.level),
                color = levelColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = getLevelTitle(context, currentLevel.level),
                color = GoldLight,
                fontFamily = PlayfairFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(Modifier.height(18.dp))

            // Прогресс-бар
            val dreamsWord = if (dreams % 100 in 11..19) pluralMany
                else when (dreams % 10) { 1 -> pluralOne; in 2..4 -> pluralFew; else -> pluralMany }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$dreams $dreamsWord", color = Color.White, fontSize = 13.sp)
                Text(
                    text = if (nextLevel != null)
                        stringResource(R.string.achievements_next_level, getLevelTitle(context, nextLevel.level), nextLevel.minDreams - dreams)
                    else stringResource(R.string.achievements_max_level),
                    color = Color.White.copy(0.65f), fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(0.12f))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Brush.horizontalGradient(listOf(GoldDark, GoldLight, GoldGlow)))
                )
            }

            Spacer(Modifier.height(18.dp))

            // Все уровни в ряд
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                dreamLevels.forEach { level ->
                    val isReached = dreams >= level.minDreams
                    val isCurrent = level.level == currentLevel.level
                    val c = hexToColorSafe(level.color)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 40.dp else 30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isReached) Brush.radialGradient(listOf(c.copy(0.35f), Color.Transparent))
                                    else Brush.radialGradient(listOf(Color.White.copy(0.05f), Color.Transparent))
                                )
                                .then(if (isCurrent) Modifier.border(1.dp, c.copy(0.70f), CircleShape) else Modifier)
                                .alpha(if (isReached) 1f else 0.28f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(level.emoji, fontSize = if (isCurrent) 18.sp else 13.sp)
                        }
                        if (isCurrent) {
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(4.dp).clip(CircleShape).background(c))
                        }
                    }
                }
            }
        }
    }
}

// ─── Карточка достижения ──────────────────────────────────────────────────────
@Composable
fun AchievementsCard(achievement: Achievement, isUnlocked: Boolean) {
    val context = LocalContext.current

    // Анимация появления при разблокировке
    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.97f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) CardGradientUnlocked else CardGradient)
            .border(
                width = if (isUnlocked) 1.dp else 0.5.dp,
                brush = if (isUnlocked)
                    Brush.verticalGradient(listOf(GoldLight.copy(0.55f), GoldDark.copy(0.20f)))
                else
                    Brush.verticalGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.04f))),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Золотое свечение сверху у разблокированных
        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(GoldLight.copy(0.12f), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Эмодзи в кружке
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked)
                            Brush.radialGradient(listOf(GoldLight.copy(0.20f), Color.Transparent))
                        else
                            Brush.radialGradient(listOf(Color.White.copy(0.05f), Color.Transparent))
                    )
                    .then(if (isUnlocked) Modifier.border(1.dp, GoldLight.copy(0.35f), CircleShape) else Modifier)
                    .alpha(if (isUnlocked) 1f else 0.22f),
                contentAlignment = Alignment.Center
            ) {
                Text(achievement.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = getAchievementTitle(context, achievement.id),
                fontSize = 10.sp,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                color = if (isUnlocked) Color.White else Color.White.copy(0.28f),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )

            if (isUnlocked) {
                Spacer(Modifier.height(5.dp))
                // Золотой индикатор разблокировки
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.horizontalGradient(listOf(GoldDark, GoldLight)))
                )
            }
        }
    }
}

// ─── Вспомогательная функция стрика (без изменений) ──────────────────────────
fun computeStreak(dreams: List<com.dreamjournal.journalofdream.model.Dream>): Int {
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
        if (key in dates) { streak++; cal.add(java.util.Calendar.DAY_OF_MONTH, -1) }
        else break
    }
    return streak
}

// Алиасы для совместимости
@Composable
fun LevelCard(dreams: Int, currentLevel: DreamLevel, nextLevel: DreamLevel?) {
    val pluralOne  = stringResource(R.string.plural_dreams_one)
    val pluralFew  = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    AchievementsLevelCard(dreams, currentLevel, nextLevel, pluralOne, pluralFew, pluralMany)
}

@Composable
fun AchievementCard(achievement: Achievement, isUnlocked: Boolean) = AchievementsCard(achievement, isUnlocked)
