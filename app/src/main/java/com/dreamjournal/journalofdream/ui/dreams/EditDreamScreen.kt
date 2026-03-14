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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.ui.dreams.CategoryManagerDialog
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

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
    val dreamIdInt = dreamId.toIntOrNull() ?: 0

    val dreamWithLocationsLD = dreamViewModel.getDreamWithLocationsById(dreamIdInt)
    val dreamWithLocationsState by dreamWithLocationsLD.observeAsState()

    val allLocations by locationViewModel.locations.observeAsState(emptyList())
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())

    val keyboardController = LocalSoftwareKeyboardController.current

    dreamWithLocationsState?.let { dreamWithLocs ->
        val dream = dreamWithLocs.dream

        var title by remember { mutableStateOf(dream.title) }
        var content by remember { mutableStateOf(dream.content) }
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var categoryMenuExpanded by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showTitleError by remember { mutableStateOf(false) }
        var showExitDialog by remember { mutableStateOf(false) }

        // Есть ли несохранённые изменения
        val hasUnsavedChanges = title != dream.title || content != dream.content

        // Перехват кнопки Назад — если есть изменения показываем диалог
        BackHandler(enabled = hasUnsavedChanges) {
            showExitDialog = true
        }

        // Диалог подтверждения выхода без сохранения
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(stringResource(R.string.discard_title)) },
                text = { Text(stringResource(R.string.discard_dream_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    }) {
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

        LaunchedEffect(categories) {
            if (selectedCategory == null && categories.isNotEmpty()) {
                selectedCategory = categories.find { it.name == dream.category }
            }
        }

        var isLocationDialogOpen by remember { mutableStateOf(false) }
        val selectedLocationIds = remember { mutableStateListOf<Int>() }

        var selectedTime by remember { mutableStateOf(dream.time.ifBlank { getCurrentTime() }) }
        var showTimePicker by remember { mutableStateOf(false) }

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

        LaunchedEffect(dreamWithLocs) {
            selectedLocationIds.clear()
            selectedLocationIds.addAll(dreamWithLocs.locations.map { it.id })
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.dream_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (hasUnsavedChanges) showExitDialog = true
                            else navController.popBackStack()
                        }) {
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
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
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
                                .heightIn(min = 120.dp),
                            minLines = 5,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    text = selectedCategory?.name?.let {
                                        localizeCategory(it,
                                            stringResource(R.string.dreams_no_category),
                                            stringResource(R.string.cat_nightmares),
                                            stringResource(R.string.cat_lucid),
                                            stringResource(R.string.cat_plot),
                                            stringResource(R.string.cat_personal))
                                    } ?: stringResource(R.string.dreams_no_category),
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                                DropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.dreams_no_category)) },
                                        onClick = { selectedCategory = null; categoryMenuExpanded = false }
                                    )
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(localizeCategory(category.name,
                                                stringResource(R.string.dreams_no_category),
                                                stringResource(R.string.cat_nightmares),
                                                stringResource(R.string.cat_lucid),
                                                stringResource(R.string.cat_plot),
                                                stringResource(R.string.cat_personal))) },
                                            onClick = { selectedCategory = category; categoryMenuExpanded = false }
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

                        Button(
                            onClick = { isLocationDialogOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = if (selectedLocationIds.isEmpty())
                                    stringResource(R.string.dream_pick_locations)
                                else
                                    stringResource(R.string.dream_locations_selected, selectedLocationIds.size),
                                color = Color.White
                            )
                        }

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
                                                        if (isSelected) selectedLocationIds.remove(location.id)
                                                        else selectedLocationIds.add(location.id)
                                                    }
                                                    .padding(8.dp)
                                            ) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = {
                                                        if (isSelected) selectedLocationIds.remove(location.id)
                                                        else selectedLocationIds.add(location.id)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.dialog_delete_dream_title)) },
                text = { Text("\"${dream.title}\" ${stringResource(R.string.dialog_delete_dream_message)}") },
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
        Box(modifier = Modifier.fillMaxSize())
    }
}
