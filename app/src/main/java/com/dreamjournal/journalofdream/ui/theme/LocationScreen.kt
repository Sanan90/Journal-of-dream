package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.LocationWithDreams
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

// Варианты сортировки
enum class LocationSort {
    BY_DREAMS,    // По количеству снов (популярные)
    BY_NAME,      // По алфавиту
    BY_LAST_DREAM // По дате последнего сна
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val locationsWithDreams by locationViewModel.getAllLocationsWithDreams().observeAsState(emptyList())
    val syncError by locationViewModel.syncError.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentSort by remember { mutableStateOf(LocationSort.BY_DREAMS) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            locationViewModel.clearSyncError()
        }
    }

    // Применяем сортировку
    val sortedLocations = remember(locationsWithDreams, currentSort) {
        when (currentSort) {
            LocationSort.BY_DREAMS ->
                locationsWithDreams.sortedByDescending { it.dreams.size }
            LocationSort.BY_NAME ->
                locationsWithDreams.sortedBy { it.location.name.lowercase() }
            LocationSort.BY_LAST_DREAM ->
                locationsWithDreams.sortedByDescending { lwd ->
                    lwd.dreams.maxOfOrNull { it.date } ?: ""
                }
        }
    }

    val sortLabel = when (currentSort) {
        LocationSort.BY_DREAMS -> context.getString(R.string.locations_sort_popular)
        LocationSort.BY_NAME -> context.getString(R.string.locations_sort_alpha)
        LocationSort.BY_LAST_DREAM -> context.getString(R.string.locations_sort_last)
    }

    // Анимация стрелки меню
    val arrowRotation by animateFloatAsState(
        targetValue = if (showSortMenu) 180f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "arrow"
    )

    var pendingDelete by remember { mutableStateOf<LocationWithDreams?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.locations_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("addLocation") }) {
                        Icon(Icons.Default.Add, stringResource(R.string.locations_add), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            if (locationsWithDreams.isEmpty()) {
                // Пустое состояние
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗺️", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.locations_empty),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.locations_empty_hint),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Строка сортировки
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.locations_total, locationsWithDreams.size),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )

                            Box {
                                TextButton(
                                    onClick = { showSortMenu = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        sortLabel,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp).rotate(arrowRotation)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_popular)) },
                                        onClick = {
                                            currentSort = LocationSort.BY_DREAMS
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == LocationSort.BY_DREAMS)
                                                Text("✓", color = Color(0xFF7E57C2))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_alpha)) },
                                        onClick = {
                                            currentSort = LocationSort.BY_NAME
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == LocationSort.BY_NAME)
                                                Text("✓", color = Color(0xFF7E57C2))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_last)) },
                                        onClick = {
                                            currentSort = LocationSort.BY_LAST_DREAM
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == LocationSort.BY_LAST_DREAM)
                                                Text("✓", color = Color(0xFF7E57C2))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Карточки локаций
                    items(sortedLocations, key = { it.location.id }) { lwd ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    pendingDelete = lwd
                                }
                                false // не удаляем сразу — ждём подтверждения
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                // Иконка масштабируется когда тянем достаточно далеко
                                val scale by animateFloatAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                                    label = "scale"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .background(
                                            color = Color.Red.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(end = 24.dp)
                                            .scale(scale)
                                            .size(28.dp)
                                    )
                                }
                            }
                        ) {
                            LocationCard(
                                locationWithDreams = lwd,
                                onClick = { navController.navigate("viewLocation/${lwd.location.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
    // Диалог подтверждения удаления
    pendingDelete?.let { lwd ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_location_title)) },
            text = {
                val count = lwd.dreams.size
                Text(
                    "\"${lwd.location.name}\" будет удалена." +
                    if (count > 0) "\n\n$count ${dreamWord(count, pluralOne, pluralFew, pluralMany)} останутся, но привязка к локации исчезнет."
                    else ""
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    locationViewModel.deleteLocation(lwd.location)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.btn_delete), color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun LocationCard(
    locationWithDreams: LocationWithDreams,
    onClick: () -> Unit
) {
    val dreamCount = locationWithDreams.dreams.size
    val location = locationWithDreams.location
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)

    // Последняя дата сна для подзаголовка
    val lastDreamDate = locationWithDreams.dreams
        .mapNotNull { it.date.ifBlank { null } }
        .maxOrNull()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2D1B69),  // тёмно-фиолетовый
                            Color(0xFF1A1040)   // почти чёрный с фиолетовым
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Декоративный акцент справа вверху
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7E57C2).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Левая часть: название + дата
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name.ifEmpty { "(Без названия)" },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (lastDreamDate != null) stringResource(R.string.locations_last_dream, lastDreamDate)
                           else stringResource(R.string.locations_no_dreams),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(12.dp))

            // Правая часть: счётчик снов в кружке
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (dreamCount > 0) Color(0xFF7E57C2).copy(alpha = 0.35f)
                        else Color.White.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$dreamCount",
                        color = if (dreamCount > 0) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dreamWord(dreamCount, pluralOne, pluralFew, pluralMany),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        } // закрываем Row
        } // закрываем внешний Box
    }
}

// Правильное склонение слова "сон"
fun dreamWord(count: Int, one: String, few: String, many: String): String {
    val lastTwo = count % 100
    val lastOne = count % 10
    return when {
        lastTwo in 11..19 -> many
        lastOne == 1 -> one
        lastOne in 2..4 -> few
        else -> many
    }
}
