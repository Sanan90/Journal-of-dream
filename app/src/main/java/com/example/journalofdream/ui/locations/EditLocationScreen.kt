package com.example.journalofdream.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.LocationViewModel

@Composable
fun EditLocationScreen(navController: NavHostController, locationId: Int, locationViewModel: LocationViewModel) {
    val location = locationViewModel.allLocations.value?.find { it.id == locationId }
    var locationName by remember { mutableStateOf(TextFieldValue(location?.name ?: "")) }
    var locationDescription by remember { mutableStateOf(TextFieldValue(location?.description ?: "")) }
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

            // Поле для редактирования имени локации
            BasicTextField(
                value = locationName,
                onValueChange = { locationName = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для редактирования описания локации
            BasicTextField(
                value = locationDescription,
                onValueChange = { locationDescription = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Сохранить изменения"
            Button(
                onClick = {
                    locationViewModel.updateLocation(
                        location!!.copy(
                            name = locationName.text,
                            description = locationDescription.text
                        )
                    )
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
                    locationViewModel.deleteLocation(location!!)
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Удалить")
            }
        }
    }
}
