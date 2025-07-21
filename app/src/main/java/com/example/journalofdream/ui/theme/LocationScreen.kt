package com.example.journalofdream.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.common.CustomButton
import com.example.journalofdream.ui.common.TopBar
import com.example.journalofdream.viewmodel.LocationViewModel
import com.example.journalofdream.model.Location

/**
 * Экран списка локаций текущего пользователя.
 * Отображает все локации, добавленные пользователем (или гостем), и позволяет перейти к их просмотру/редактированию.
 */
@Composable
fun LocationListScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    // Наблюдаем за списком локаций из ViewModel
    val locationList by locationViewModel.locations.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                onSaveClick = { navController.navigate("addLocation") }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(locationList) { loc ->
                        LocationItem(
                            location = loc,
                            onClick = {
                                // При клике на локацию переходим к её просмотру (с возможностью редактирования)
                                navController.navigate("viewLocation/${loc.id}")
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun LocationItem(location: Location, onClick: () -> Unit) {
    CustomButton(
        text = location.name.ifEmpty { "(Без названия)" },
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    )
}
