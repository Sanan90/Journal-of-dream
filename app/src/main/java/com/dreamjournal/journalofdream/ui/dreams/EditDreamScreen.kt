package com.dreamjournal.journalofdream.ui.dreams

import com.dreamjournal.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.ui.dreams.CategoryManagerDialog
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

/**
 * Экран редактирования существующего сна.
 * @param dreamId – идентификатор сна, переданный через NavHost (аргумент маршрута "editDream/{id}").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDreamScreen(
    navController: NavHostController,
    dreamId: String,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    // Преобразуем идентификатор из строки в Int (если не получилось, ставим 0)
    val dreamIdInt = dreamId.toIntOrNull() ?: 0

    // Получаем LiveData сна с его локациями из ViewModel
    val dreamWithLocationsLD = dreamViewModel.getDreamWithLocationsById(dreamIdInt)
    val dreamWithLocationsState by dreamWithLocationsLD.observeAsState()

    // Список всех локаций пользователя (для выбора новых/удаления старых привязок)
    val allLocations by locationViewModel.locations.observeAsState(emptyList())
    // Список категорий (для выпадающего списка категорий)
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())

    val keyboardController = LocalSoftwareKeyboardController.current

    // Если данные сна ещё загружаются (dreamWithLocations == null), можно показать индикатор загрузки или пустое пространство
    dreamWithLocationsState?.let { dreamWithLocs ->
        val dream = dreamWithLocs.dream

        // Поля состояния с начальными значениями из загруженного сна
        var title by remember { mutableStateOf(dream.title) }
        var content by remember { mutableStateOf(dream.content) }

        // Инициализация выбранной категории
        // ИСПРАВЛЕНО: используем LaunchedEffect, т.к. при первом remember categories ещё пустой список
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var categoryMenuExpanded by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showTitleError by remember { mutableStateOf(false) }

        LaunchedEffect(categories) {
            if (selectedCategory == null && categories.isNotEmpty()) {
                selectedCategory = categories.find { it.name == dream.category }
            }
        }

        // Состояния для управления выбором локаций
        var isLocationDialogOpen by remember { mutableStateOf(false) }
        val selectedLocationIds = remember { mutableStateListOf<Int>() }

        // Время сна — берём из существующего или текущее
        var selectedTime by remember { mutableStateOf(dream.time.ifBlank { getCurrentTime() }) }
        var showTimePicker by remember { mutableStateOf(false) }

        // TimePickerDialog
        if (showTimePicker) {
            val timeParts = selectedTime.split(":").map { it.toIntOrNull() ?: 0 }
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    showTimePicker = false
                },
                timeParts.getOrElse(0) { 0 },
                timeParts.getOrElse(1) { 0 },
                true
            ).also {
                it.setOnCancelListener { showTimePicker = false }
                it.show()
            }
        }

        // ИСПРАВЛЕНО: при загрузке сна заполняем список выбранных локаций (IDs) для корректного отображения
        LaunchedEffect(dreamWithLocs) {
            selectedLocationIds.clear()
            selectedLocationIds.addAll(dreamWithLocs.locations.map { it.id })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.dream_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.btn_back),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.btn_delete),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        // Поля ввода с текущими значениями сна
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (it.isNotBlank()) showTitleError = false
                            },
                            label = { Text(stringResource(R.string.dream_field_name)) },
                            textStyle = TextStyle(fontSize = 18.sp),
                            singleLine = true,
                            isError = showTitleError,
                            supportingText = if (showTitleError) {
                                { Text(stringResource(R.string.dream_field_name)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text(stringResource(R.string.dream_field_desc)) },
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
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
                                    .clickable { categoryMenuExpanded = true }
                                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = selectedCategory?.name?.let { localizeCategory(it, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal)) } ?: stringResource(R.string.dreams_no_category),
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                DropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.dreams_no_category)) },
                                        onClick = {
                                            selectedCategory = null
                                            categoryMenuExpanded = false
                                        }
                                    )
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(localizeCategory(category.name, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))) },
                                            onClick = {
                                                selectedCategory = category
                                                categoryMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { showCategoryManager = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.dream_manage_categories),
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

                        // Кнопка выбора локаций (открывает диалог)
                        Button(
                            onClick = { isLocationDialogOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = if (selectedLocationIds.isEmpty()) stringResource(R.string.dream_pick_locations) else stringResource(R.string.dream_locations_selected, selectedLocationIds.size),
                                color = Color.White
                            )
                        }

                        // Диалог выбора локаций (аналогичен AddDreamScreen)
                        if (isLocationDialogOpen) {
                            AlertDialog(
                                onDismissRequest = { isLocationDialogOpen = false },
                                title = { Text(stringResource(R.string.dream_pick_locations)) },
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

                        // Дата и время сна
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Дата — только отображение (менять дату в редактировании не даём)
                            OutlinedButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = formatDateForDisplay(dream.date),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                            // Время — можно менять
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(0.7f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Text("🕐 $selectedTime", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Кнопка сохранения изменений
                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    showTitleError = true
                                    return@Button
                                }
                                val updatedDream = dream.copy(
                                    title = title.trim(),
                                    content = content.trim(),
                                    time = selectedTime,
                                    category = selectedCategory?.name ?: context.getString(R.string.dreams_no_category)
                                )
                                dreamViewModel.updateDream(updatedDream, selectedLocationIds.toList())
                                keyboardController?.hide()
                                navController.popBackStack()
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(stringResource(R.string.dream_save_changes), color = Color.White)
                        }
                    }
                }
            }
        )

        // Диалог подтверждения удаления сна
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.dialog_delete_dream_title)) },
                text = { Text("\"${dream.title}\" будет удалён. Это действие нельзя отменить.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        dreamViewModel.deleteDream(dream)
                        navController.popBackStack()
                    }) {
                        Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    } ?: run {
        // Если сон не найден или ещё не загружен, можно вывести пустой экран или индикатор
        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder для состояния загрузки/отсутствия данных
        }
    }
}
