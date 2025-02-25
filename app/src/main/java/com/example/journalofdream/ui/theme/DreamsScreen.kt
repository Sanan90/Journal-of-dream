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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.model.Dream
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.model.Category
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.viewmodel.CategoryViewModel
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamsScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {

    // Вместо allDreams используем dreamViewModel.dreams
    // т. к. мы в DreamViewModel сделали val dreams: LiveData<List<Dream>>
    val allDreams by dreamViewModel.dreams.observeAsState(listOf())

    // Получаем CategoryViewModel для списка категорий
    val categoryViewModel: CategoryViewModel = viewModel()
    val categories by categoryViewModel.allCategories.observeAsState(listOf())

    // Переменные для выбора категории и поиска
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
                    // Кнопка "Добавить сон"
                    IconButton(onClick = { navController.navigate("addDream") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(Color.Transparent)
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Поле поиска
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Поиск", color = Color.White)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = Color.White
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedLeadingIconColor = Color.White,
                            unfocusedLeadingIconColor = Color.White,
                        ),
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true
                    )

                    // Поле выбора категории
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { expanded = true }
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
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
                            // Пункт "Все сны"
                            DropdownMenuItem(
                                text = { Text("Все сны") },
                                onClick = {
                                    selectedCategory = null
                                    expanded = false
                                }
                            )
                            // Список категорий
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

                    // Список снов
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Фильтруем
                        val filteredDreams = allDreams.filter { dream ->
                            // Если категория выбрана, dream.category == выбранной
                            (selectedCategory == null || dream.category == selectedCategory?.name) &&
                                    // Поиск в title или content
                                    (dream.title.contains(searchQuery.text, ignoreCase = true) ||
                                            dream.content.contains(searchQuery.text, ignoreCase = true))
                        }

                        items(filteredDreams) { dream ->
                            DreamListItem(dream, navController)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DreamListItem(dream: Dream, navController: NavHostController) {
    val previewText = dream.title

    Button(
        onClick = {
            navController.navigate("editDream/${dream.id}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = previewText,
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
