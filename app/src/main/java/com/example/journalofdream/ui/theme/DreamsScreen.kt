package com.example.journalofdream.ui.theme

import com.example.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
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
import androidx.compose.runtime.saveable.rememberSaveable
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

// monthNames объявляется внутри composable через getMonthNames()

// Конвертирует любой формат даты в "yyyy-MM" для группировки
fun dateToMonthKey(date: String): String {
    return try {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            // Уже в формате yyyy-MM-dd
            date.substring(0, 7)
        } else {
            // Старый формат dd.MM.yyyy
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            String.format("%04d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
        }
    } catch (e: Exception) { "0000-00" }
}

// Конвертирует любой формат даты в "yyyy-MM-dd" для корректной сортировки
fun dateToSortKey(date: String): String {
    return try {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            date
        } else {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            String.format("%04d-%02d-%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH))
        }
    } catch (e: Exception) { "0000-00-00" }
}

fun formatMonthKey(key: String, monthNames: List<String>): String {
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
    val context = LocalContext.current
    val monthNames = listOf(
        stringResource(R.string.month_jan), stringResource(R.string.month_feb), stringResource(R.string.month_mar),
        stringResource(R.string.month_apr), stringResource(R.string.month_may), stringResource(R.string.month_jun),
        stringResource(R.string.month_jul), stringResource(R.string.month_aug), stringResource(R.string.month_sep),
        stringResource(R.string.month_oct), stringResource(R.string.month_nov), stringResource(R.string.month_dec)
    )
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
    // Сортировка в режиме обычного списка: true = по дате, false = по категории
    var sortByDate by rememberSaveable { mutableStateOf(true) }

    // Выбранный месяц (если открыт) — null = показываем сетку
    // rememberSaveable сохраняет значение при возврате с экрана просмотра сна
    var openedMonth by rememberSaveable { mutableStateOf<String?>(null) }

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
    val allMonthKeys = remember(allDreams) {
        allDreams
            .map { dream -> dateToMonthKey(dream.date) }
            .toSortedSet(compareByDescending { it })
    }

    // Количество снов в каждом месяце с учётом фильтра (категория + поиск)
    val dreamsByMonth = remember(searchResults) {
        searchResults.groupBy { dream -> dateToMonthKey(dream.date) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    // Если открыт месяц — показываем его название
                    Text(
                        text = if (openedMonth != null) formatMonthKey(openedMonth!!, monthNames)
                               else stringResource(R.string.dreams_list),
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
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                actions = {
                    if (openedMonth == null) {
                        TextButton(onClick = { monthMode = !monthMode }) {
                            Text(
                                text = if (monthMode) stringResource(R.string.dreams_list) else stringResource(R.string.dreams_months),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        // Кнопка сортировки — только в режиме обычного списка
                        if (!monthMode) {
                            TextButton(onClick = { sortByDate = !sortByDate }) {
                                Text(
                                    text = if (sortByDate) stringResource(R.string.dreams_sort_az) else stringResource(R.string.dreams_sort_date),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        // Кнопка сортировки внутри открытого месяца
                        TextButton(onClick = { sortByDate = !sortByDate }) {
                            Text(
                                text = if (sortByDate) stringResource(R.string.dreams_sort_az) else stringResource(R.string.dreams_sort_date),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate("addDream") }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_dream), tint = Color.White)
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
                        placeholder = { Text(stringResource(R.string.dreams_search), color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.dreams_search), tint = Color.White)
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
                            text = selectedCategory?.name?.let { localizeCategory(it, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal)) } ?: stringResource(R.string.dreams_all),
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dreams_all)) },
                                onClick = { selectedCategory = null; categoryExpanded = false }
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(localizeCategory(category.name, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))) },
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
                                Text(stringResource(R.string.dreams_empty_month), color = Color.White, fontSize = 16.sp)
                            }
                        } else {
                            val sortedInMonth = remember(dreamsInMonth, sortByDate) {
                                if (sortByDate) {
                                    dreamsInMonth.sortedByDescending { dateToSortKey(it.date) }
                                } else {
                                    dreamsInMonth.sortedWith(
                                        compareBy(
                                            { it.category.ifBlank { context.getString(R.string.dreams_no_category) } },
                                            { dateToSortKey(it.date) }
                                        )
                                    )
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(4.dp)) }
                                if (!sortByDate) {
                                    val grouped = sortedInMonth.groupBy {
                                        it.category.ifBlank { context.getString(R.string.dreams_no_category) }
                                    }
                                    grouped.forEach { (category, dreams) ->
                                        item(key = "cat_month_$category") {
                                            Text(
                                                text = category,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(
                                                    top = 8.dp, start = 4.dp, bottom = 4.dp
                                                )
                                            )
                                        }
                                        items(items = dreams, key = { it.localId }) { dream ->
                                            DreamListItem(
                                                dream = dream,
                                                navController = navController,
                                                onDelete = { dreamViewModel.deleteDream(it) },
                                                categoryColor = categoryColorMap[dream.category]
                                            )
                                        }
                                    }
                                } else {
                                    items(items = sortedInMonth, key = { it.localId }) { dream ->
                                        DreamListItem(
                                            dream = dream,
                                            navController = navController,
                                            onDelete = { dreamViewModel.deleteDream(it) },
                                            categoryColor = categoryColorMap[dream.category]
                                        )
                                    }
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
                                            stringResource(R.string.dreams_not_found)
                                        else
                                            stringResource(R.string.dreams_empty),
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                    if (searchQuery.text.isEmpty() && selectedCategory == null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.dreams_empty_hint),
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
                                        monthNames = monthNames,
                                        onClick = { openedMonth = monthKey }
                                    )
                                }
                            }
                        } else {
                            // Обычный список с сортировкой
                            val sortedResults = remember(searchResults, sortByDate) {
                                if (sortByDate) {
                                    searchResults.sortedByDescending { dateToSortKey(it.date) }
                                } else {
                                    searchResults.sortedWith(
                                        compareBy(
                                            { it.category.ifBlank { context.getString(R.string.dreams_no_category) } },
                                            { dateToSortKey(it.date) }
                                        )
                                    )
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Если сортировка по категории — показываем заголовки групп
                                if (!sortByDate) {
                                    val grouped = sortedResults.groupBy {
                                        it.category.ifBlank { context.getString(R.string.dreams_no_category) }
                                    }
                                    grouped.forEach { (category, dreams) ->
                                        item(key = "cat_$category") {
                                            Text(
                                                text = category,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(
                                                    top = 12.dp,
                                                    start = 4.dp,
                                                    bottom = 4.dp
                                                )
                                            )
                                        }
                                        items(
                                            items = dreams,
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
                                } else {
                                    items(
                                        items = sortedResults,
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
}

@Composable
fun MonthCard(
    monthKey: String,
    count: Int,
    monthNames: List<String>,
    onClick: () -> Unit
) {
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
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
                text = formatMonthKey(monthKey, monthNames),
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
                    text = pluralDreams(count, pluralOne, pluralFew, pluralMany),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
