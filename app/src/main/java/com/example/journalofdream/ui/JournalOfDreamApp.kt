// Файл: com/example/journalofdream/ui/JournalOfDreamApp.kt

package com.example.journalofdream.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.journalofdream.ui.theme.AppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.journalofdream.ui.dreams.*
import com.example.journalofdream.ui.locations.*
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.auth.AuthScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.journalofdream.R
import com.example.journalofdream.ui.theme.DreamsScreen
import com.example.journalofdream.ui.theme.LocationListScreen
import com.example.journalofdream.ui.theme.MainScreen
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun JournalOfDreamApp() {
    AppTheme {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val navController = rememberNavController()
        val dreamViewModel: DreamViewModel = viewModel()
        val locationViewModel: LocationViewModel = viewModel()
        val auth = FirebaseAuth.getInstance()
        val currentUser = remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
        val skipAuth = remember { mutableStateOf(false) }

        // Читаем skipAuth из SharedPreferences при запуске приложения
        LaunchedEffect(Unit) {
            skipAuth.value = sharedPreferences.getBoolean("skipAuth", false)
        }

        // Настраиваем GoogleSignInClient
        val googleSignInClient: GoogleSignInClient = remember {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
        }

        // Обработка состояния авторизации
        if (currentUser.value != null || skipAuth.value) {
            // Пользователь авторизован или вошел как гость
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    MainScreen(navController, onLogout = {
                        // Функция выхода
                        // Выход из FirebaseAuth, если пользователь авторизован
                        if (auth.currentUser != null) {
                            auth.signOut()
                        }
                        // Выход из GoogleSignInClient
                        googleSignInClient.signOut()

                        // Сбрасываем currentUser
                        currentUser.value = null

                        // Сбрасываем skipAuth и сохраняем в SharedPreferences
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()

                        // Навигация на экран авторизации
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    })
                }
                composable("dreams") { DreamsScreen(navController, dreamViewModel) }
                composable("addDream") { AddDreamScreen(navController, dreamViewModel) }
                composable("editDream/{id}") { backStackEntry ->
                    val dreamId = backStackEntry.arguments?.getString("id")
                    if (dreamId != null) {
                        EditDreamScreen(navController, dreamId, dreamViewModel)
                    }
                }
                composable("locations") { LocationListScreen(navController, locationViewModel) }
                composable("addLocation") { AddLocationScreen(navController, locationViewModel) }
                composable("editLocation/{id}") { backStackEntry ->
                    val locationId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                    if (locationId != null) {
                        EditLocationScreen(navController, locationId, locationViewModel)
                    }
                }
                composable("viewLocation/{locationId}") { backStackEntry ->
                    val locationId = backStackEntry.arguments?.getString("locationId")?.toIntOrNull()
                    if (locationId != null) {
                        ViewLocationScreen(navController, locationId, locationViewModel)
                    }
                }
                // Удаляем повторный composable("auth"), так как экран авторизации обрабатывается вне NavHost
            }
        } else {
            // Пользователь не авторизован
            AuthScreen(
                onAuthSuccess = {
                    currentUser.value = auth.currentUser
                    // Сбрасываем skipAuth и сохраняем в SharedPreferences
                    skipAuth.value = false
                    sharedPreferences.edit().putBoolean("skipAuth", false).apply()
                },
                onSkipAuth = {
                    skipAuth.value = true
                    sharedPreferences.edit().putBoolean("skipAuth", true).apply()
                }
            )
        }
    }
}
