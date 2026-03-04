package com.example.journalofdream.ui.locations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.dreams.DreamListItem
import com.example.journalofdream.viewmodel.LocationViewModel

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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) "Редактирование" else location.name,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isEditing = false
                                nameError = false
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = if (isEditing) "Отмена" else "Назад",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (!isEditing) {
                            IconButton(onClick = {
                                nameInput = location.name
                                descInput = location.description
                                isEditing = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Редактировать",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    if (!isEditing) {
                        // Режим просмотра
                        if (location.description.isNotBlank()) {
                            Text(
                                text = location.description,
                                style = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Удалить локацию", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Связанные сны:",
                            style = TextStyle(color = Color.White, fontSize = 18.sp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Пункт M — пустое состояние связанных снов
                        if (dreams.isEmpty()) {
                            Text(
                                text = "У этой локации пока нет связанных снов",
                                style = TextStyle(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            LazyColumn {
                                items(dreams) { dream ->
                                    DreamListItem(dream, navController)
                                }
                            }
                        }

                    } else {
                        // Режим редактирования
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.isNotBlank()) nameError = false
                            },
                            label = { Text("Название локации") },
                            textStyle = TextStyle(fontSize = 18.sp),
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text("Введите название локации") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Описание локации") },
                            textStyle = TextStyle(fontSize = 16.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (nameInput.isBlank()) {
                                    nameError = true
                                    return@Button
                                }
                                val updatedLoc = location.copy(
                                    name = nameInput.trim(),
                                    description = descInput.trim()
                                )
                                locationViewModel.updateLocation(updatedLoc)
                                isEditing = false
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text("Сохранить", color = Color.White)
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить локацию?") },
                text = { Text("\"${location.name}\" будет удалена. Связанные сны останутся, но привязка к локации исчезнет.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        locationViewModel.deleteLocation(location)
                        navController.popBackStack()
                    }) {
                        Text("Удалить", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
