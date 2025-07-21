package com.example.journalofdream.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.theme.DreamListItem
import com.example.journalofdream.viewmodel.LocationViewModel

/**
 * Экран для просмотра одной локации + связанных снов.
 * Позволяет редактировать поля локации непосредственно здесь (isEditing),
 * или удалять локацию.
 */
@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val locationWithDreamsLD = locationViewModel.getLocationWithDreams(locationId)
    val locationWithDreamsState = locationWithDreamsLD.observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    locationWithDreamsState.value?.let { locWithDreams ->
        val location = locWithDreams.location
        val dreams = locWithDreams.dreams

        // Если сейчас не редактируем, инициализируем поля из location
        if (!isEditing) {
            name = location.name
            description = location.description
        }

        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            // Кнопка \"Назад\" в правом верхнем углу
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
            ) {
                Text("Назад", color = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                // Отображение названия и описания локации
                Text(
                    text = name,
                    style = TextStyle(color = Color.White, fontSize = 24.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = description,
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Кнопки \"Редактировать\" и \"Удалить\"
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(onClick = { isEditing = true }) {
                        Text("Редактировать")
                    }
                    Button(
                        onClick = {
                            locationViewModel.deleteLocation(location)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Удалить")
                    }
                }

                // Если включен режим редактирования
                if (isEditing) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Название локации",
                        style = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    var nameInput by remember { mutableStateOf(name) }
                    BasicTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Описание локации",
                        style = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    var descInput by remember { mutableStateOf(description) }
                    BasicTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка \"Сохранить изменения\"
                    Button(
                        onClick = {
                            val updatedLoc = location.copy(
                                name = nameInput,
                                description = descInput
                            )
                            locationViewModel.updateLocation(updatedLoc)
                            // Обновляем локальные переменные
                            name = nameInput
                            description = descInput
                            isEditing = false
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Сохранить изменения")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Список снов, связанных с этой локацией
                Text(
                    text = "Связанные сны:",
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Лист снов
                LazyColumn {
                    items(dreams) { dream ->
                        DreamListItem(dream, navController)
                    }
                }
            }
        }
    } ?: run {
        // Если locationWithDreamsState.value == null
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
