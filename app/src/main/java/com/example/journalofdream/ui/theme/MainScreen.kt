package com.example.journalofdream.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen

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
    displayName: String?
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Анимация заголовка при первом появлении
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isGuest) {
                        // Если гость — показываем кнопку "Авторизуйтесь"
                        TextButton(onClick = { navController.navigate("auth") }) {
                            Text("Авторизуйтесь")
                        }
                    } else {
                        // Иначе пишем "Сновидец: ..."
                        Text("Сновидец: ${displayName ?: "Неизвестно"}")
                    }
                },
                actions = {
                    // Иконка выхода (видна только если не гость)
                    if (!isGuest) {
                        IconButton(onClick = onLogout) {
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
                        modifier = Modifier.padding(bottom = 32.dp),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Кнопка для перехода на экран "Список снов"
                Button(
                    onClick = { navController.navigate("dreams") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                        Text("Записать сновидение", color = Color.White, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка для перехода на экран "Локации"
                Button(
                    onClick = { navController.navigate("locations") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                        Text("Локации", color = Color.White, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Дополнительная кнопка (например, "Техники")
                Button(
                    onClick = {
                        // пока нет экрана
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                                        Color(0xFF1B5E20).copy(alpha = 0.5f),
                                        Color(0xFF2E7D32).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Техники", color = Color.White, fontSize = 24.sp)
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
            }
        }
    }
}
