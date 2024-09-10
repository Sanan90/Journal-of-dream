package com.example.journalofdream.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


// Правильные импорты для Material3
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import com.example.journalofdream.ui.common.BackgroundScreen

@Composable
fun MainScreen(navController: NavHostController) {
    var isVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) } // Для отображения диалога выбора

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // Показать диалог при нажатии на кнопку
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить запись")
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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
