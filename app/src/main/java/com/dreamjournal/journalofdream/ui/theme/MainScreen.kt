package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.platform.LocalContext
import com.dreamjournal.journalofdream.model.getLevelTitle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.getLevelForCount
import com.dreamjournal.journalofdream.model.hexToColorSafe
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.ui.theme.DreamButtonEnd
import com.dreamjournal.journalofdream.ui.theme.DreamButtonStart
import com.dreamjournal.journalofdream.ui.theme.LocationButtonEnd
import com.dreamjournal.journalofdream.ui.theme.LocationButtonStart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.ui.theme.TechButtonEnd
import com.dreamjournal.journalofdream.ui.theme.TechButtonStart
import com.dreamjournal.journalofdream.R

/**
 * Главный экран приложения.
 *
 * @param navController NavHostController для навигации.
 * @param onLogout колбэк выхода из аккаунта, где вызываются методы
 *                 dreamViewModel.onUserLogout() и locationViewModel.onUserLogout().
 * @param isGuest true, если пользователь зашёл как "guest".
 * @param displayName отображаемое имя (или e-mail), если пользователь авторизован.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    isGuest: Boolean,
    displayName: String?,
    dreamViewModel: DreamViewModel = viewModel()
) {
    val allDreams by dreamViewModel.dreams.observeAsState(emptyList())
    val currentLevel = remember(allDreams) { getLevelForCount(allDreams.size) }
    val context = LocalContext.current
    val pluralOne  = stringResource(R.string.plural_dreams_one)
    val pluralFew  = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val recordedStr = stringResource(R.string.main_dreams_recorded)
    var isVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Анимация заголовка при первом появлении
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isGuest) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.main_guest),
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { navController.navigate("auth") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_login),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    } else {
                        // Иначе пишем "Сновидец: ..."
                        Text(stringResource(R.string.main_dreamer, displayName ?: stringResource(R.string.main_guest)))
                    }
                },
                actions = {
                    // Кнопка настроек
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                    // Иконка выхода (видна только если не гость)
                    if (!isGuest) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = stringResource(R.string.btn_logout)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // Светящаяся кнопка с анимацией пульсации
            val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )
            Box(contentAlignment = Alignment.Center) {
                // Ореол свечения
                Box(
                    modifier = Modifier
                        .size(72.dp * pulseScale)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF7E57C2).copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF9C27B0), Color(0xFF3F51B5))
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.btn_add_dream),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Фон
            BackgroundScreen()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(0.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Анимированный заголовок
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f)
                ) {
                    Text(
                        text = stringResource(R.string.nav_diary),
                        color = Color.Gray.copy(alpha = 0.95f),
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Мини-виджет текущего уровня
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn()
                ) {
                    val levelColor = hexToColorSafe(currentLevel.color)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(1.dp, levelColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { navController.navigate("achievements") }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(currentLevel.emoji, fontSize = 20.sp)
                        Column {
                            Text(
                                text = stringResource(R.string.main_level, currentLevel.level, getLevelTitle(context, currentLevel.level)),
                                color = levelColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${allDreams.size} ${pluralDreams(allDreams.size, pluralOne, pluralFew, pluralMany)} $recordedStr",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка для перехода на экран "Список снов"
                Button(
                    onClick = { navController.navigate("dreams") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        DreamButtonStart.copy(alpha = 0.5f),
                                        DreamButtonEnd.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.nav_diary), color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка для перехода на экран "Локации"
                Button(
                    onClick = { navController.navigate("locations") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        LocationButtonStart.copy(alpha = 0.5f),
                                        LocationButtonEnd.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.nav_locations), color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("characters") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6D4C41).copy(alpha = 0.5f),
                                        Color(0xFF8D6E63).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.nav_characters), color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Дополнительная кнопка (например, "Техники")
                Button(
                    onClick = {
                        navController.navigate("techniques")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        TechButtonStart.copy(alpha = 0.5f),
                                        TechButtonEnd.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.nav_techniques), color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопки Статистика, График и Достижения
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка Статистика
                    Button(
                        onClick = { navController.navigate("stats") },
                        modifier = Modifier
                            .weight(1f)
                            .height(75.dp)
                            .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF1A237E).copy(alpha = 0.5f),
                                            Color(0xFF283593).copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📊", fontSize = 22.sp)
                                Text(stringResource(R.string.nav_stats), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Кнопка График
                    Button(
                        onClick = { navController.navigate("chart") },
                        modifier = Modifier
                            .weight(1f)
                            .height(75.dp)
                            .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00695C).copy(alpha = 0.5f),
                                            Color(0xFF00897B).copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📈", fontSize = 22.sp)
                                Text(stringResource(R.string.nav_chart), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Кнопка Достижения
                    Button(
                        onClick = { navController.navigate("achievements") },
                        modifier = Modifier
                            .weight(1f)
                            .height(75.dp)
                            .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF4A148C).copy(alpha = 0.5f),
                                            Color(0xFF6A1B9A).copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🏆", fontSize = 22.sp)
                                Text(stringResource(R.string.nav_achievements), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Диалог выбора "Добавить сон или локацию?"
                if (showDialog) {
                    ChooseActionDialog(
                        onDismiss = { showDialog = false },
                        onDreamSelected = {
                            navController.navigate("addDream")
                            showDialog = false
                        },
                        onLocationSelected = {
                            navController.navigate("addLocation")
                            showDialog = false
                        },
                        onCharacterSelected = {
                            navController.navigate("addCharacter")
                            showDialog = false
                        }
                    )
                }

                // Диалог подтверждения выхода из аккаунта
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text(stringResource(R.string.dialog_logout_title)) },
                        text = { Text(stringResource(R.string.dialog_logout_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                onLogout()
                            }) {
                                Text(stringResource(R.string.btn_logout), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}
