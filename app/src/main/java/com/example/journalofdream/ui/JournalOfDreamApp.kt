package com.example.journalofdream.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.journalofdream.ui.theme.AppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.journalofdream.ui.dreams.*
import com.example.journalofdream.ui.locations.*
import com.example.journalofdream.ui.theme.MainScreen
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.theme.DreamsScreen
import com.example.journalofdream.ui.theme.LocationListScreen

@Composable
fun JournalOfDreamApp() {
    AppTheme {
        val navController = rememberNavController()
        val dreamViewModel: DreamViewModel = viewModel()
        val locationViewModel: LocationViewModel = viewModel()

        // Навигация приложения
        NavHost(navController, startDestination = "main") {
            composable("main") { MainScreen(navController) }
            composable("dreams") { DreamsScreen(navController, dreamViewModel) }
            composable("addDream") { AddDreamScreen(navController, dreamViewModel) }
            composable("editDream/{id}") { backStackEntry ->
                val dreamId = backStackEntry.arguments?.getString("id")?.toInt()
                if (dreamId != null) {
                    EditDreamScreen(navController, dreamId, dreamViewModel)
                }
            }
            composable("locations") { LocationListScreen(navController, locationViewModel) }
            composable("addLocation") { AddLocationScreen(navController, locationViewModel) }
            composable("editLocation/{id}") { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("id")?.toInt()
                if (locationId != null) {
                    EditLocationScreen(navController, locationId, locationViewModel)
                }
            }
            composable("viewLocation/{locationId}") { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")?.toInt()
                    ?: return@composable
                ViewLocationScreen(navController, locationId, locationViewModel)
            }
        }
    }
}
