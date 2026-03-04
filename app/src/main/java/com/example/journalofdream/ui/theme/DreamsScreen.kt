package com.example.journalofdream.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
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
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.dreams.DreamListItem
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val allDreams by dreamViewModel.dreams.observeAsState(emptyList())
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())
    val categoryColorMap = remember(categories) {
        categories.associate { it.name to it.color }
    }
    val syncError by dreamViewModel.syncError.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Показываем Snackbar при ошибке синхронизации
    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            dreamViewModel.clearSyncError()
        }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // ИСПРАВЛЕНО: поиск через DAO с debounce 300мс — не дёргаем базу на каждый символ
    val searchResults by produceState(
        initialValue = allDreams,
        key1 = searchQuery.text,
        key2 = selectedCategory,
        key3 = allDreams
    ) {
        if (searchQuery.text.isBlank() && selectedCategory == null) {
            value = allDreams
        } else {
            delay(300) // debounce
            value = allDreams.filter { dream ->
                (selectedCategory == null || dream.category == selectedCategory?.name) &&
                (searchQuery.text.isBlank() ||
                    dream.title.contains(searchQuery.text, ignoreCase = true) ||
                    dream.content.contains(searchQuery.text, ignoreCase = true))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    .imePadding()
            ) {
                // Поле поиска
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

                // Фильтр по категории
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
                        DropdownMenuItem(
                            text = { Text("Все сны") },
                            onClick = { selectedCategory = null; expanded = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; expanded = false }
                            )
                        }
                    }
                }

                // Список снов или пустое состояние
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🌙", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.text.isNotEmpty() || selectedCategory != null)
                                    "Ничего не найдено"
                                else
                                    "У вас пока нет снов",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            if (searchQuery.text.isEmpty() && selectedCategory == null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Нажмите + чтобы добавить первый сон",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = searchResults,
                            key = { it.localId }
                        ) { dream ->
                            DreamListItem(
                                dream = dream,
                                navController = navController,
                                onDelete = { dreamViewModel.deleteDream(it) },
                                categoryColor = categoryColorMap[dream.category]
                            )
                        }
                    }
                }
            }
        }
    }
}
