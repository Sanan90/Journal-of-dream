package com.example.journalofdream.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.LocationViewModel

@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val location by locationViewModel.getLocationById(locationId).observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    location?.let {
        if (!isEditing) {
            name = it.name
            description = it.description
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        // Кнопка "Назад" в правом верхнем углу
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

            // Отображение названия и описания локации без редактирования
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

            // Кнопки "Редактировать" и "Удалить"
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Button(onClick = { isEditing = true }) {
                    Text("Редактировать")
                }

                Button(
                    onClick = {
                        location?.let {
                            locationViewModel.deleteLocation(it)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Удалить")
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Название локации",
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
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

                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка "Сохранить изменения"
                Button(
                    onClick = {
                        val updatedLocation = location?.copy(
                            name = name,
                            description = description
                        )
                        if (updatedLocation != null) {
                            locationViewModel.updateLocation(updatedLocation)
                        }
                        isEditing = false // Завершаем режим редактирования
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
            location?.let {
                val relatedDreams by locationViewModel.getDreamsByLocation(it.id).observeAsState(listOf())
                relatedDreams.forEach { dream ->
                    Text(text = "${dream.date}: ${dream.title}", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

