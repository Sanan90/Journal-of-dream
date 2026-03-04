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
import androidx.compose.material.icons.filled.Add
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
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.dreams.CategoryManagerDialog
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDreamScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    // Поля ввода для нового сна
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // Состояния для выбора локаций
    var isLocationDialogOpen by remember { mutableStateOf(false) }
    val selectedLocationIds = remember { mutableStateListOf<Int>() }

    // ViewModel для категорий (если требуется)
    val categoryViewModel: CategoryViewModel = viewModel()
    val categories by categoryViewModel.allCategories.observeAsState(listOf())

    // Список всех локаций текущего пользователя (для отображения в диалоге)
    val allLocations by locationViewModel.locations.observeAsState(emptyList())

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
                // Фоновое изображение/цвет
                BackgroundScreen()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Поле ввода заголовка сна
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) showError = false
                        },
                        label = { Text("Название сна") },
                        textStyle = TextStyle(fontSize = 18.sp),
                        singleLine = true,
                        isError = showError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Поле ввода содержания сна
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Описание сна") },
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { /* скрыть клавиатуру */ })
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Выбор категории + управление категориями
                    var showCategoryManager by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isCategoryMenuExpanded = true }
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = selectedCategory?.name ?: "Без категории",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            DropdownMenu(
                                expanded = isCategoryMenuExpanded,
                                onDismissRequest = { isCategoryMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Без категории") },
                                    onClick = {
                                        selectedCategory = null
                                        isCategoryMenuExpanded = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategory = cat
                                            isCategoryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Кнопка управления категориями (добавить/удалить)
                        IconButton(onClick = { showCategoryManager = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Управление категориями",
                                tint = Color.White
                            )
                        }
                    }

                    if (showCategoryManager) {
                        CategoryManagerDialog(
                            categoryViewModel = categoryViewModel,
                            onDismiss = { showCategoryManager = false },
                            onCategorySelected = { selectedCategory = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка для выбора локаций
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

                    // Диалог со списком локаций для выбора
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

                    // Сообщение об ошибке валидации
                    if (showError) {
                        Text(
                            text = "Введите название сна",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Кнопка сохранения нового сна
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showError = true
                                return@Button
                            }
                            showError = false
                            // Создаём объект Dream из введённых данных
                            val dream = Dream(
                                title = title.trim(),
                                content = content.trim(),
                                date = getCurrentDate(),
                                category = selectedCategory?.name ?: "Без категории"
                            )
                            // Сохраняем сон вместе с выбранными локациями
                            dreamViewModel.addDream(dream, selectedLocationIds.toList())
                            // Возврат к списку после сохранения
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
