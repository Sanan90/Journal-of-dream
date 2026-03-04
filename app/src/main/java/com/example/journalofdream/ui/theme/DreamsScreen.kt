package com.example.journalofdream.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Category
import com.example.journalofdream.model.Dream
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.dreams.DreamListItem
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel
import kotlinx.coroutines.delay

private val monthNames = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

fun formatMonthKey(key: String): String {
    return try {
        val parts = key.split("-")
        val year = parts[0]
        val month = parts[1].toInt() - 1
        "${monthNames[month]} $year"
    } catch (e: Exception) { key }
}

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

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            dreamViewModel.clearSyncError()
        }
    }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var monthMode by remember { mutableStateOf(true) }

    // Выбранный месяц (если открыт) — null = показываем сетку
    var openedMonth by remember { mutableStateOf<String?>(null) }

    // Перехватываем кнопку назад на телефоне
    BackHandler(enabled = openedMonth != null) {
        openedMonth = null
    }

    val searchResults by produceState(
        initialValue = allDreams,
        key1 = searchQuery.text,
        key2 = selectedCategory,
        key3 = allDreams
    ) {
        if (searchQuery.text.isBlank() && selectedCategory == null) {
            value = allDreams
        } else {
            delay(300)
            value = allDreams.filter { dream ->
                (selectedCategory == null || dream.category == selectedCategory?.name) &&
                (searchQuery.text.isBlank() ||
                    dream.title.contains(searchQuery.text, ignoreCase = true) ||
                    dream.content.contains(searchQuery.text, ignoreCase = true))
            }
        }
    }

    // Все месяцы — строятся из ВСЕХ снов (не фильтруются)
    // чтобы квадратики не исчезали при выборе категории
    val allMonthKeys = remember(allDreams) {
        allDreams
            .map { dream -> try { dream.date.substring(0, 7) } catch (e: Exception) { "0000-00" } }
            .toSortedSet(compareByDescending { it })
    }

    // Количество снов в каждом месяце с учётом фильтра (категория + поиск)
    val dreamsByMonth = remember(searchResults) {
        searchResults
            .groupBy { dream ->
                try { dream.date.substring(0, 7) } catch (e: Exception) { "0000-00" }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    // Если открыт месяц — показываем его название
                    Text(
                        text = if (openedMonth != null) formatMonthKey(openedMonth!!)
                               else "Список сновидений",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (openedMonth != null) {
                            openedMonth = null // закрываем месяц, возвращаемся к сетке
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                actions = {
                    if (openedMonth == null) {
                        TextButton(onClick = { monthMode = !monthMode }) {
                            Text(
                                text = if (monthMode) "Список" else "Месяцы",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate("addDream") }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить сон", tint = Color.White)
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
                // Поиск и фильтр — всегда видны
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            // При поиске — переключаемся в список и закрываем месяц
                            if (it.text.isNotBlank()) {
                                openedMonth = null
                                monthMode = false
                            }
                        },
                        placeholder = { Text("Поиск", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Поиск", tint = Color.White)
                        },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { categoryExpanded = true }
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedCategory?.name ?: "Все сны",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Все сны") },
                                onClick = { selectedCategory = null; categoryExpanded = false }
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { selectedCategory = category; categoryExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Контент
                AnimatedContent(
                    targetState = openedMonth,
                    transitionSpec = {
                        if (targetState != null) {
                            // Открываем месяц — влетает снизу
                            slideInVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                initialOffsetY = { it }
                            ) + fadeIn() togetherWith
                            slideOutVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                targetOffsetY = { -it / 3 }
                            ) + fadeOut()
                        } else {
                            // Закрываем — сетка возвращается
                            slideInVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                initialOffsetY = { -it / 3 }
                            ) + fadeIn() togetherWith
                            slideOutVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                targetOffsetY = { it }
                            ) + fadeOut()
                        }
                    },
                    label = "monthTransition"
                ) { month ->
                    if (month != null) {
                        // ── СПИСОК СНОВ ВЫБРАННОГО МЕСЯЦА ──
                        // Фильтруем по категории и поиску внутри месяца
                        val dreamsInMonth = (dreamsByMonth[month] ?: emptyList()).let { list ->
                            list.filter { dream ->
                                (selectedCategory == null || dream.category == selectedCategory?.name) &&
                                (searchQuery.text.isBlank() ||
                                    dream.title.contains(searchQuery.text, ignoreCase = true) ||
                                    dream.content.contains(searchQuery.text, ignoreCase = true))
                            }
                        }
                        if (dreamsInMonth.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Нет снов за этот месяц", color = Color.White, fontSize = 16.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(4.dp)) }
                                items(
                                    items = dreamsInMonth,
                                    key = { it.localId }
                                ) { dream ->
                                    DreamListItem(
                                        dream = dream,
                                        navController = navController,
                                        onDelete = { dreamViewModel.deleteDream(it) },
                                        categoryColor = categoryColorMap[dream.category]
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    } else {
                        // ── СЕТКА МЕСЯЦЕВ или ОБЫЧНЫЙ СПИСОК ──
                        if (searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🌙", fontSize = 64.sp)
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
                        } else if (monthMode) {
                            // Сетка квадратиков
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                gridItems(allMonthKeys.toList()) { monthKey ->
                                    val count = dreamsByMonth[monthKey]?.size ?: 0
                                    MonthCard(
                                        monthKey = monthKey,
                                        count = count,
                                        onClick = { openedMonth = monthKey }
                                    )
                                }
                            }
                        } else {
                            // Обычный список
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}

@Composable
fun MonthCard(
    monthKey: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatMonthKey(monthKey),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Column {
                Text(
                    text = count.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pluralDreams(count),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
