package com.example.journalofdream.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Location
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.common.CustomButton
import com.example.journalofdream.ui.common.TopBar
import com.example.journalofdream.viewmodel.LocationViewModel

@Composable
fun LocationListScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    val locationList by locationViewModel.locations.observeAsState(emptyList())
    val syncError by locationViewModel.syncError.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            locationViewModel.clearSyncError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                navController = navController,
                onSaveClick = { navController.navigate("addLocation") }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                if (locationList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🗺️", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "У вас пока нет локаций",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нажмите + чтобы добавить первую локацию",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
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
                                    navController.navigate("viewLocation/${loc.id}")
                                }
                            )
                        }
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
