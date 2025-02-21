// Файл: com/example/journalofdream/ui/theme/MainScreen.kt

package com.example.journalofdream.ui.theme
import com.example.journalofdream.ui.theme.ChooseActionDialog

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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import androidx.compose.material3.Scaffold


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    isGuest: Boolean,        // Гость или авторизованный пользователь
    displayName: String?     // Имя пользователя или email
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) } // Для отображения диалога выбора

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Если гость - показываем кнопку «Авторизуйтесь»
                    // Иначе - показываем «Сновидец: (displayName)»
                    if (isGuest) {
                        TextButton(
                            onClick = {
                                // Действие при нажатии: можно сразу navController.navigate("auth")
                                // Но вы писали, что пока не хотите «авторизовываться без выхода».
                                // Можно просто ничего не делать, или перейти на экран auth:
                                navController.navigate("auth")
                            }
                        ) {
                            Text("Авторизуйтесь")
                        }
                    } else {
                        // Отображаем имя пользователя
                        Text("Сновидец: ${displayName ?: "Неизвестно"}")
                    }
                },
                actions = {
                    // Можно добавить иконку выхода, но только если пользователь НЕ гость
                    if (!isGuest) {
                        IconButton(onClick = { onLogout() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Выйти"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Показать диалог при нажатии на кнопку
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить запись"
                )
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(0.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Анимированный заголовок приложения
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

                    // Кнопка для перехода на экран сновидений
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
                            Text(
                                "Записать сновидение",
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка для перехода на экран локаций
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
                            Text(
                                "Локации",
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка для перехода на экран техник (пока не реализована)
                    Button(
                        onClick = {
                            // Действие для третьей кнопки (техники)
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
                            Text(
                                "Техники",
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Диалог выбора действия
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
    )
}
