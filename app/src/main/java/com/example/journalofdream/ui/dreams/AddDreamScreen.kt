package com.example.journalofdream.ui.dreams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Category
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Scaffold


fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDreamScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {
    // Состояния для полей ввода
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    // Состояние для выбора локаций
    var isLocationDialogOpen by remember { mutableStateOf(false) }
    val selectedLocationIds = remember { mutableStateListOf<Int>() }

    // 1. Получаем CategoryViewModel
    val categoryViewModel: CategoryViewModel = viewModel()

    // 2. Наблюдаем за списком категорий
    val categories by categoryViewModel.allCategories.observeAsState(listOf())

    val allLocations by dreamViewModel.allLocations.observeAsState(listOf())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить сон") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Фоновое изображение
                BackgroundScreen()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Поле заголовка
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Заголовок", color = Color.White) },
                        placeholder = { Text("Введите заголовок", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Поле содержания
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Содержание", color = Color.White) },
                        placeholder = { Text("Введите содержание сна", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp
                        ),
                        maxLines = Int.MAX_VALUE,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Default
                        ),
                        keyboardActions = KeyboardActions.Default
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка выбора категории
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        Button(
                            onClick = { isCategoryMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = selectedCategory?.name ?: "Выбрать категорию",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        DropdownMenu(
                            expanded = isCategoryMenuExpanded,
                            onDismissRequest = { isCategoryMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        isCategoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка выбора локаций
                    Button(
                        onClick = { isLocationDialogOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = if (selectedLocationIds.isEmpty()) "Выбрать локации" else "Локации выбраны: ${selectedLocationIds.size}",
                            color = Color.White
                        )
                    }

                    // Диалог выбора локаций
                    if (isLocationDialogOpen) {
                        AlertDialog(
                            onDismissRequest = { isLocationDialogOpen = false },
                            title = { Text("Выберите локации") },
                            text = {
                                LazyColumn {
                                    items(allLocations) { location ->
                                        val isSelected = selectedLocationIds.contains(location.id)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isSelected) {
                                                        selectedLocationIds.remove(location.id)
                                                    } else {
                                                        selectedLocationIds.add(location.id)
                                                    }
                                                }
                                                .padding(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    if (isSelected) {
                                                        selectedLocationIds.remove(location.id)
                                                    } else {
                                                        selectedLocationIds.add(location.id)
                                                    }
                                                }
                                            )
                                            Text(text = location.name)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { isLocationDialogOpen = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка сохранения
                    Button(
                        onClick = {
                            val dream = Dream(
                                title = title,
                                content = content,
                                date = getCurrentDate(),
                                category = selectedCategory?.name ?: "Без категории"
                            )
                            // Сохраняем сон вместе с выбранными локациями
                            dreamViewModel.addDream(dream, selectedLocationIds)
                            navController.popBackStack()
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(text = "Сохранить", color = Color.White)
                    }
                }
            }
        }
    )
}
