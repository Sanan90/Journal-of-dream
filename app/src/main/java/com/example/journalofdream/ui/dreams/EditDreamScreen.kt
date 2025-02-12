// Файл: com/example/journalofdream/ui/dreams/EditDreamScreen.kt

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDreamScreen(
    navController: NavHostController,
    dreamId: String,
    dreamViewModel: DreamViewModel
) {
    val dreamWithLocations by dreamViewModel.getDreamWithLocationsById(dreamId).observeAsState()
    val categories by dreamViewModel.categories.observeAsState(listOf())
    val allLocations by dreamViewModel.allLocations.observeAsState(listOf())

    val keyboardController = LocalSoftwareKeyboardController.current

    dreamWithLocations?.let { dreamWithLocs ->
        val dream = dreamWithLocs.dream
        var title by remember { mutableStateOf(dream.title) }
        var content by remember { mutableStateOf(dream.content) }
        var selectedCategory by remember { mutableStateOf<Category?>(categories.find { it.name == dream.category }) }
        var isCategoryMenuExpanded by remember { mutableStateOf(false) }
        var isLocationDialogOpen by remember { mutableStateOf(false) }

        // Инициализируем выбранные локации
        val selectedLocationIds = remember { mutableStateListOf<Int>() }
        LaunchedEffect(dreamWithLocs) {
            selectedLocationIds.clear()
            selectedLocationIds.addAll(dreamWithLocs.locations.map { it.id })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Редактировать сон") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            dreamViewModel.deleteDream(dream)
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
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
                                unfocusedLabelColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
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
                                unfocusedLabelColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
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

                        // Кнопка сохранения изменений
                        Button(
                            onClick = {
                                val updatedDream = dream.copy(
                                    title = title,
                                    content = content,
                                    category = selectedCategory?.name ?: "Без категории"
                                )
                                dreamViewModel.updateDream(updatedDream, selectedLocationIds)
                                keyboardController?.hide()
                                navController.popBackStack()
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                        ) {
                            Text("Сохранить изменения", color = Color.White)
                        }
                    }
                }
            }
        )
    } ?: run {
        // Индикатор загрузки или сообщение об ошибке
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
