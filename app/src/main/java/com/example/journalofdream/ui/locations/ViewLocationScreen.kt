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
fun ViewLocationScreen(navController: NavHostController, locationId: Int, locationViewModel: LocationViewModel = viewModel()) {
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

            if (isEditing) {
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

                Button(onClick = {
                    val updatedLocation = location?.copy(name = name, description = description)
                    if (updatedLocation != null) {
                        locationViewModel.updateLocation(updatedLocation)
                    }
                    isEditing = false
                }) {
                    Text("Сохранить изменения")
                }
            }
        }
    }
}
