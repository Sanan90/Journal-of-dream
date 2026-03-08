package com.example.journalofdream.ui.dreams

import androidx.activity.compose.BackHandler
import com.example.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
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
import androidx.compose.material.icons.filled.DateRange
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
import com.example.journalofdream.util.rememberSpeechLauncher
import com.example.journalofdream.ui.dreams.CategoryManagerDialog
import com.example.journalofdream.viewmodel.CategoryViewModel
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getCurrentTime(): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date())
}

fun formatDateForDisplay(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        outputFormat.format(inputFormat.parse(date)!!)
    } catch (e: Exception) {
        date // если формат старый — показываем как есть
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDreamScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    // Поля ввода для нового сна
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(getCurrentTime()) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Есть ли несохранённые изменения
    val hasUnsavedChanges = title.isNotBlank() || content.isNotBlank()

    // Перехват кнопки Назад
    BackHandler(enabled = hasUnsavedChanges) {
        showExitDialog = true
    }

    // Диалог подтверждения выхода
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_dream_message)) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; navController.popBackStack() }) {
                    Text(stringResource(R.string.discard_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.discard_dismiss))
                }
            }
        )
    }

    // DatePickerDialog
    if (showDatePicker) {
        val cal = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                selectedDate = formatted
                showDatePicker = false
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).also {
            it.setOnCancelListener { showDatePicker = false }
            it.show()
        }
    }

    // TimePickerDialog
    if (showTimePicker) {
        val cal = java.util.Calendar.getInstance()
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            },
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            true // 24-часовой формат
        ).also {
            it.setOnCancelListener { showTimePicker = false }
            it.show()
        }
    }

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
                title = { Text(stringResource(R.string.dream_add_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (hasUnsavedChanges) showExitDialog = true else navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
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
                        label = { Text(stringResource(R.string.dream_field_name)) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        singleLine = true,
                        isError = showError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Выбор даты и времени сна в одну строку
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Дата
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.dream_pick_date),
                                tint = Color.White,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = formatDateForDisplay(selectedDate),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        // Время
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(0.7f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Text("🕐 $selectedTime", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Поле ввода содержания сна + кнопка голосового ввода
                    val speechLauncher = rememberSpeechLauncher { recognized ->
                        content = if (content.isBlank()) recognized
                                  else "$content $recognized"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text(stringResource(R.string.dream_field_desc)) },
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { })
                        )
                        // Кнопка микрофона
                        IconButton(
                            onClick = { speechLauncher() },
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(48.dp)
                                .background(
                                    Color(0xFF7E57C2).copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Text("🎤", fontSize = 22.sp)
                        }
                    }

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
                                text = selectedCategory?.name?.let { localizeCategory(it, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal)) } ?: stringResource(R.string.dreams_no_category),
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            DropdownMenu(
                                expanded = isCategoryMenuExpanded,
                                onDismissRequest = { isCategoryMenuExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.dreams_no_category)) },
                                    onClick = {
                                        selectedCategory = null
                                        isCategoryMenuExpanded = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(localizeCategory(cat.name, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))) },
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

                    // Кнопка для выбора локаций
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

                    // Диалог со списком локаций для выбора
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

                    // Сообщение об ошибке валидации
                    if (showError) {
                        Text(
                            text = stringResource(R.string.dream_error_name),
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
                                date = selectedDate,
                                time = selectedTime,
                                category = selectedCategory?.name ?: context.getString(R.string.dreams_no_category)
                            )
                            // Сохраняем сон вместе с выбранными локациями
                            dreamViewModel.addDream(dream, selectedLocationIds.toList())
                            // Возврат к списку после сохранения
                            navController.popBackStack()
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(text = stringResource(R.string.btn_save), color = Color.White)
                    }
                }
            }
        }
    )
}
