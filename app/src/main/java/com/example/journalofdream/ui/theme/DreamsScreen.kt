package com.example.journalofdream.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Category
import com.example.journalofdream.model.Dream
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel

/**
 * Экран списка снов.
 * Отображает все сны текущего пользователя (или гостя) с возможностью фильтрации.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // Получаем список всех снов (LiveData наблюдается как State)
    val allDreams by dreamViewModel.dreams.observeAsState(emptyList())

    // Список категорий (для фильтрации по категории)
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())

    // Состояния для выбранной категории фильтра и строки поиска
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список сновидений", color = Color.White) },
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
                    // Кнопка добавления нового сна
                    IconButton(onClick = { navController.navigate("addDream") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить сон",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Поле поиска по тексту сна
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск", color = Color.White) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск", tint = Color.White)
                    },
                    textStyle = TextStyle(color = Color.White),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(4.dp)
                )

                // Выбор категории (DropdownMenu для фильтрации)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { expanded = true }
                        .background(Color.White.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedCategory?.name ?: "Все сны",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Пункт для выбора всех категорий
                        DropdownMenuItem(
                            text = { Text("Все сны") },
                            onClick = {
                                selectedCategory = null
                                expanded = false
                            }
                        )
                        // Список категорий для фильтрации
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Формируем отфильтрованный список с учётом категории и строки поиска
                val filteredDreams = allDreams.filter { dream ->
                    (selectedCategory == null || dream.category == selectedCategory?.name) &&
                            (dream.title.contains(searchQuery.text, ignoreCase = true) ||
                                    dream.content.contains(searchQuery.text, ignoreCase = true))
                }

                // Список снов
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredDreams) { dream ->
                        DreamListItem(dream, navController)
                    }
                }
            }
        }
    }
}

/**
 * Отдельный элемент списка сна.
 * При нажатии переходит на экран редактирования выбранного сна.
 */
@Composable
fun DreamListItem(dream: Dream, navController: NavHostController) {
    Button(
        onClick = {
            // Переходим на экран редактирования сна (передаём localId сна в маршрут)
            navController.navigate("editDream/${dream.localId}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dream.title,
                color = Color.Black,
                fontSize = 18.sp,
                maxLines = 1
            )
            Text(
                text = dream.date,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
