package com.example.journalofdream.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.common.CustomButton
import com.example.journalofdream.ui.common.TopBar
import com.example.journalofdream.model.Location
import com.example.journalofdream.viewmodel.LocationViewModel

@Composable
fun LocationListScreen(navController: NavHostController, locationViewModel: LocationViewModel) {
    val allLocations by locationViewModel.allLocations.observeAsState(listOf())

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
                    items(allLocations) { location ->
                        CustomButton(
                            text = location.name,
                            onClick = { navController.navigate("viewLocation/${location.id}") }
                        )
                    }
                }
            }
        }
    )
}
