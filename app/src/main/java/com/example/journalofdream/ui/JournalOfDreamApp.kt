package com.example.journalofdream.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.journalofdream.ui.dreams.*
import com.example.journalofdream.ui.locations.*
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import com.example.journalofdream.viewmodel.CategoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.ui.auth.AuthScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.journalofdream.R
import com.example.journalofdream.ui.theme.AppTheme
import com.example.journalofdream.ui.theme.DreamsScreen
import com.example.journalofdream.ui.theme.LocationListScreen
import com.example.journalofdream.ui.theme.MainScreen
import com.example.journalofdream.ui.theme.StatsScreen
import com.example.journalofdream.ui.theme.TechniquesScreen
import com.google.android.gms.auth.api.signin.GoogleSignInClient

/**
 * Главный composable приложения, где определяются NavHost и навигация между экранами.
 * Определяет, показывать ли экран авторизации или основной контент, в зависимости от состояния (guest или авторизован).
 */
@Composable
fun JournalOfDreamApp() {
    AppTheme {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Флаг, указывающий, что пользователь решил пропустить авторизацию (guest mode)
        val skipAuth = remember { mutableStateOf(sharedPreferences.getBoolean("skipAuth", false)) }

        // NavController для навигации по экранам
        val navController = rememberNavController()

        // Инициализируем ViewModel-ы (живут на уровне Activity)
        val dreamViewModel: DreamViewModel = viewModel()
        val locationViewModel: LocationViewModel = viewModel()
        val categoryViewModel: CategoryViewModel = viewModel()

        // FirebaseAuth – определяем текущего авторизованного пользователя, если есть
        val auth = FirebaseAuth.getInstance()
        val currentUser = remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

        // Клиент для выхода из Google (если вход был через Google аккаунт)
        val googleSignInClient: GoogleSignInClient = remember {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
        }

        // Определяем, гость ли текущий пользователь
        val isGuest = skipAuth.value

        // Для отображения имени/почты текущего пользователя (в заголовке MainScreen)
        val userName = currentUser.value?.displayName ?: currentUser.value?.email

        // Определяем стартовый экран: если пользователь уже авторизован или выбрал guest-режим, идём на main, иначе на auth
        NavHost(
            navController = navController,
            startDestination = if (currentUser.value != null || isGuest) "main" else "auth"
        ) {
            // Экран авторизации
            composable("auth") {
                AuthScreen(
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel,
                    onAuthSuccess = {
                        currentUser.value = auth.currentUser
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()
                        // Переключаем категории на текущего пользователя
                        auth.currentUser?.uid?.let { categoryViewModel.setOwner(it) }
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onSkipAuth = {
                        skipAuth.value = true
                        sharedPreferences.edit().putBoolean("skipAuth", true).apply()
                        categoryViewModel.setOwner("guest")
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Главный экран приложения
            composable("main") {
                MainScreen(
                    navController = navController,
                    onLogout = {
                        // Обработка выхода из аккаунта
                        if (auth.currentUser != null) {
                            // 1. Останавливаем синхронизацию и переключаем данные на guest в DreamViewModel
                            dreamViewModel.onUserLogout()
                            // 2. То же делаем для LocationViewModel (ИСПРАВЛЕНО: добавлено для немедленного обновления списка локаций)
                            locationViewModel.onUserLogout()
                            // 3. Выходим из аккаунта FirebaseAuth и Google (если было)
                            googleSignInClient.signOut()
                            auth.signOut()
                            // 4. Сбрасываем сохранённое состояние авторизации
                            currentUser.value = null
                            skipAuth.value = false
                            sharedPreferences.edit().putBoolean("skipAuth", false).apply()
                            // 5. Переключаем категории на гостевые
                            categoryViewModel.setOwner("guest")
                        }
                        // Переходим на экран авторизации, очищая backstack
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    isGuest = isGuest,
                    displayName = userName
                )
            }

            // Экран списка снов
            composable("dreams") {
                DreamsScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel
                )
            }

            // Экран добавления сна
            composable("addDream") {
                AddDreamScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel
                )
            }

            // Экран редактирования сна (с параметром id)
            composable("editDream/{id}") { backStackEntry ->
                val dreamId = backStackEntry.arguments?.getString("id") ?: "0"
                EditDreamScreen(
                    navController = navController,
                    dreamId = dreamId,
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel
                )
            }

            // Экран списка локаций
            composable("locations") {
                LocationListScreen(
                    navController = navController,
                    locationViewModel = locationViewModel
                )
            }

            // Экран добавления новой локации
            composable("addLocation") {
                AddLocationScreen(
                    navController = navController,
                    locationViewModel = locationViewModel
                )
            }

            // Экран просмотра локации (с отображением связанных снов)
            composable("viewLocation/{locationId}") { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")?.toIntOrNull()
                if (locationId != null) {
                    ViewLocationScreen(
                        navController = navController,
                        locationId = locationId,
                        locationViewModel = locationViewModel
                    )
                }
            }

            // Экран техник осознанных сновидений
            composable("techniques") {
                TechniquesScreen(navController = navController)
            }

            // Экран статистики
            composable("stats") {
                StatsScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel
                )
            }
        }
    }
}
