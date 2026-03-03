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
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.dreams.DreamListItem
import com.example.journalofdream.viewmodel.LocationViewModel

@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val locationWithDreamsLD = locationViewModel.getLocationWithDreams(locationId)
    val locationWithDreamsState = locationWithDreamsLD.observeAsState()

    var isEditing by remember { mutableStateOf(false) }

    // ИСПРАВЛЕНО: все remember вынесены на верхний уровень composable,
    // а не внутрь if(isEditing) — это предотвращает краш
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    locationWithDreamsState.value?.let { locWithDreams ->
        val location = locWithDreams.location
        val dreams = locWithDreams.dreams

        // Инициализируем поля когда данные пришли и не в режиме редактирования
        LaunchedEffect(location) {
            if (!isEditing) {
                nameInput = location.name
                descInput = location.description
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

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

                if (!isEditing) {
                    // Режим просмотра
                    Text(
                        text = location.name,
                        style = TextStyle(color = Color.White, fontSize = 24.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = location.description,
                        style = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = {
                            nameInput = location.name
                            descInput = location.description
                            isEditing = true
                        }) {
                            Text("Редактировать")
                        }
                        Button(onClick = {
                            locationViewModel.deleteLocation(location)
                            navController.popBackStack()
                        }) {
                            Text("Удалить")
                        }
                    }
                } else {
                    // Режим редактирования
                    Text(
                        text = "Название локации",
                        style = TextStyle(color = Color.White, fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = {
                            val updatedLoc = location.copy(
                                name = nameInput.trim(),
                                description = descInput.trim()
                            )
                            locationViewModel.updateLocation(updatedLoc)
                            isEditing = false
                        }) {
                            Text("Сохранить")
                        }
                        Button(onClick = { isEditing = false }) {
                            Text("Отмена")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Связанные сны:",
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    items(dreams) { dream ->
                        DreamListItem(dream, navController)
                    }
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
