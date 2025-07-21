package com.example.journalofdream.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.LocationViewModel

/**
 * Экран для редактирования локации (locationId).
 */
@Composable
fun EditLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel
) {
    // Получаем LiveData<Location>
    val locationLD = locationViewModel.getLocationById(locationId)
    val locationState = locationLD.observeAsState()

    var locationName by remember { mutableStateOf(TextFieldValue("")) }
    var locationDescription by remember { mutableStateOf(TextFieldValue("")) }

    // Когда локация появляется/меняется:
    LaunchedEffect(locationState.value) {
        val loc = locationState.value
        if (loc != null) {
            locationName = TextFieldValue(loc.name)
            locationDescription = TextFieldValue(loc.description)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        BackgroundScreen()

        // Кнопка "Отмена"
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Text("Отмена", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            val location = locationState.value
            if (location == null) {
                Text("Загрузка локации...", color = Color.White)
                return@Column
            }

            // Поле "Название"
            BasicTextField(
                value = locationName,
                onValueChange = { locationName = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Описание"
            BasicTextField(
                value = locationDescription,
                onValueChange = { locationDescription = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Сохранить изменения"
            Button(
                onClick = {
                    val updatedLocation = location.copy(
                        name = locationName.text,
                        description = locationDescription.text
                    )
                    locationViewModel.updateLocation(updatedLocation)
                    keyboardController?.hide()
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Сохранить")
            }

            // Кнопка "Удалить"
            Button(
                onClick = {
                    locationViewModel.deleteLocation(location)
                    keyboardController?.hide()
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Удалить")
            }
        }
    }
}
