package com.example.journalofdream.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import com.example.journalofdream.model.getLevelForCount
import com.example.journalofdream.model.hexToColorSafe
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.theme.DreamButtonEnd
import com.example.journalofdream.ui.theme.DreamButtonStart
import com.example.journalofdream.ui.theme.LocationButtonEnd
import com.example.journalofdream.ui.theme.LocationButtonStart
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.ui.theme.TechButtonEnd
import com.example.journalofdream.ui.theme.TechButtonStart

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
                                text = "Гостевой режим",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { navController.navigate("auth") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Войти",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    } else {
                        // Иначе пишем "Сновидец: ..."
                        Text("Сновидец: ${displayName ?: "Неизвестно"}")
                    }
                },
                actions = {
                    // Кнопка настроек
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                    // Иконка выхода (видна только если не гость)
                    if (!isGuest) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Выйти"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // Кнопка "Добавить" (сон или локацию)
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить запись"
                )
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
                        text = "Дневник сновидений",
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
                                text = "Ур. ${currentLevel.level} · ${currentLevel.title}",
                                color = levelColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${allDreams.size} снов записано",
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
                        Text("Записать сновидение", color = Color.White, fontSize = 18.sp)
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
                        Text("Локации", color = Color.White, fontSize = 18.sp)
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
                        Text("Техники", color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопки Статистика и Достижения рядом
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                Text("📊", fontSize = 28.sp)
                                Text("Статистика", color = Color.White, fontSize = 14.sp)
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
                                Text("🏆", fontSize = 28.sp)
                                Text("Достижения", color = Color.White, fontSize = 14.sp)
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
                        }
                    )
                }

                // Диалог подтверждения выхода из аккаунта
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Выйти из аккаунта?") },
                        text = { Text("Вы уверены что хотите выйти? Локальные данные останутся на устройстве.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                onLogout()
                            }) {
                                Text("Выйти", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }
        }
    }
}
