package com.dreamjournal.journalofdream.ui.locations

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.ui.dreams.DreamListItem
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDateShort(date: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(date)!!)
    } catch (e: Exception) { date }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val locationWithDreamsState by locationViewModel.getLocationWithDreams(locationId).observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    locationWithDreamsState?.let { locWithDreams ->
        val location = locWithDreams.location
        val dreams = locWithDreams.dreams

        LaunchedEffect(location) {
            if (!isEditing) {
                nameInput = location.name
                descInput = location.description
            }
        }

        // Статистика
        val dreamCount = dreams.size
        val firstDream = dreams.minByOrNull { it.date }
        val lastDream = dreams.maxByOrNull { it.date }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) stringResource(R.string.location_edit_title) else location.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isEditing = false
                                nameError = false
                                nameInput = location.name
                                descInput = location.description
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = if (isEditing) stringResource(R.string.btn_cancel) else stringResource(R.string.btn_back),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (!isEditing) {
                            // Кнопка редактировать
                            IconButton(onClick = {
                                nameInput = location.name
                                descInput = location.description
                                isEditing = true
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = Color.White)
                            }
                            // Кнопка удалить
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.btn_delete),
                                    tint = Color(0xFFEF5350).copy(alpha = 0.9f)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                if (!isEditing) {
                    // ── РЕЖИМ ПРОСМОТРА ──────────────────────────────────────
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Описание (если есть)
                        if (location.description.isNotBlank()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Text(
                                        text = location.description,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        // Статистика — 3 плитки
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatChip(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.location_stat_dreams),
                                    value = "$dreamCount"
                                )
                                StatChip(
                                    modifier = Modifier.weight(1.6f),
                                    label = stringResource(R.string.location_stat_first),
                                    value = if (firstDream != null) formatDateShort(firstDream.date) else "—"
                                )
                                StatChip(
                                    modifier = Modifier.weight(1.6f),
                                    label = stringResource(R.string.location_stat_last),
                                    value = if (lastDream != null) formatDateShort(lastDream.date) else "—"
                                )
                            }
                        }

                        // Заголовок списка снов
                        item {
                            Text(
                                text = if (dreamCount > 0) stringResource(R.string.location_dreams_title) else stringResource(R.string.locations_no_dreams),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Пустое состояние
                        if (dreams.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.05f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌙", fontSize = 36.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                stringResource(R.string.location_dreams_empty_hint),
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 13.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Список снов через DreamListItem
                            items(dreams, key = { it.localId }) { dream ->
                                DreamListItem(dream = dream, navController = navController)
                            }
                        }
                    }

                } else {
                    // ── РЕЖИМ РЕДАКТИРОВАНИЯ ─────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.isNotBlank()) nameError = false
                            },
                            label = { Text(stringResource(R.string.location_field_name)) },
                            textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text(stringResource(R.string.location_field_name)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7E57C2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = Color(0xFF7E57C2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text(stringResource(R.string.location_field_desc_optional)) },
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7E57C2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = Color(0xFF7E57C2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (nameInput.isBlank()) {
                                    nameError = true
                                    return@Button
                                }
                                locationViewModel.updateLocation(
                                    location.copy(
                                        name = nameInput.trim(),
                                        description = descInput.trim()
                                    )
                                )
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7E57C2)
                            )
                        ) {
                            Text(stringResource(R.string.btn_save), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Диалог удаления
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.dialog_delete_location_title)) },
                text = {
                    Text(
                        "\"${location.name}\" будет удалена." +
                        if (dreamCount > 0) "\n\n$dreamCount ${dreamWord(dreamCount)} останутся, но привязка к локации исчезнет."
                        else ""
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        locationViewModel.deleteLocation(location)
                        navController.popBackStack()
                    }) {
                        Text(stringResource(R.string.btn_delete), color = Color(0xFFEF5350))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
                }
            )
        }

    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

// Плитка со статистикой
@Composable
fun StatChip(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7E57C2).copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

private fun dreamWord(count: Int): String {
    val lastTwo = count % 100
    val lastOne = count % 10
    return when {
        lastTwo in 11..19 -> "снов"
        lastOne == 1 -> "сон"
        lastOne in 2..4 -> "сна"
        else -> "снов"
    }
}
