package com.example.journalofdream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel


// Правильные импорты для Material3
import com.example.journalofdream.ui.dreams.AddDreamScreen
import com.example.journalofdream.ui.dreams.EditDreamScreen
import com.example.journalofdream.ui.locations.AddLocationScreen
import com.example.journalofdream.ui.locations.EditLocationScreen
import com.example.journalofdream.ui.locations.ViewLocationScreen
import com.example.journalofdream.ui.theme.DreamsScreen
import com.example.journalofdream.ui.theme.LocationListScreen
import com.example.journalofdream.ui.theme.MainScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val dreamViewModel: DreamViewModel = viewModel()
            val locationViewModel: LocationViewModel = viewModel()

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

}


// Превью экрана для быстрой проверки интерфейса в Android Studio
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}
