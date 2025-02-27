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

        // Читаем флаг гостевого режима (skipAuth) синхронно, чтобы избежать задержки.
        val skipAuth = remember { mutableStateOf(sharedPreferences.getBoolean("skipAuth", false)) }

        // NavController для управления навигацией между экранами.
        val navController = rememberNavController()

        // ViewModels приложения
        val dreamViewModel: DreamViewModel = viewModel()   // используется для управления записями снов
        val locationViewModel: LocationViewModel = viewModel()

        // Firebase Auth для проверки текущего пользователя (если авторизован).
        val auth = FirebaseAuth.getInstance()
        val currentUser = remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

        // Клиент GoogleSignIn (используется при выходе из Google-аккаунта)
        val googleSignInClient: GoogleSignInClient = remember {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
        }

        // Признак гостевого режима определяем по флагу skipAuth.
        val isGuest = skipAuth.value

        // Отображаемое имя пользователя (если вошёл) или email; для гостя будет null.
        val userName = currentUser.value?.displayName ?: currentUser.value?.email

        // Стартовый экран приложения:
        // Если уже есть авторизованный пользователь *или* включён гостевой режим,
        // то сразу показываем главный экран ("main"), иначе – экран авторизации ("auth").
        val startDestination = if (auth.currentUser != null || skipAuth.value) "main" else "auth"

        // Определяем навигационную структуру приложения.
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // ЭКРАН АВТОРИЗАЦИИ
            composable("auth") {
                AuthScreen(
                    dreamViewModel = dreamViewModel,   // Передаем существующий DreamViewModel, чтобы использовать общие данные
                    onAuthSuccess = {
                        // Колбэк при успешной авторизации (e-mail/пароль или Google).
                        currentUser.value = auth.currentUser
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()

                        // DreamViewModel.onUserLogin() уже был вызван в AuthScreen,
                        // поэтому локальные записи гостя перенесены в аккаунт и синхронизированы с Firestore.
                        // Теперь LiveData списка снов в DreamViewModel будет отображать сны нового пользователя.

                        // Переходим на главный экран.
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onSkipAuth = {
                        // Колбэк при выборе "Войти как гость".
                        skipAuth.value = true
                        sharedPreferences.edit().putBoolean("skipAuth", true).apply()

                        // Остаемся в гостевом режиме (ownerUid="guest").
                        // DreamViewModel продолжит работать с локальными данными гостя.
                        // Переходим на главный экран.
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // ГЛАВНЫЙ ЭКРАН
            composable("main") {
                MainScreen(
                    navController = navController,
                    onLogout = {
                        // Обработка выхода из аккаунта пользователя.
                        if (auth.currentUser != null) {
                            // 1. Вызываем выход в DreamViewModel: останавливаем синхронизацию и переключаемся на guest.
                            dreamViewModel.onUserLogout()
                        }
                        // 2. Также выходим из Google-аккаунта, если был вход через Google.
                        googleSignInClient.signOut()

                        // 3. Сбрасываем данные текущего пользователя в состоянии приложения.
                        currentUser.value = null
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()

                        // После onUserLogout() DreamViewModel.ownerUid стал "guest",
                        // и LiveData снов переключится на локальные записи гостя.
                        // Переходим на экран авторизации.
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    isGuest = isGuest,
                    displayName = userName
                )
            }

            // ОСТАЛЬНЫЕ ЭКРАНЫ (список снов, добавление/редактирование сна; список локаций и т.д.)
            composable("dreams") {
                DreamsScreen(navController, dreamViewModel)
            }
            composable("addDream") {
                AddDreamScreen(navController, dreamViewModel)
            }
            composable("editDream/{id}") { backStackEntry ->
                val dreamId = backStackEntry.arguments?.getString("id")
                if (dreamId != null) {
                    EditDreamScreen(navController, dreamId, dreamViewModel)
                }
            }
            composable("locations") {
                LocationListScreen(navController, locationViewModel)
            }
            composable("addLocation") {
                AddLocationScreen(navController, locationViewModel)
            }
            composable("editLocation/{id}") { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")?.toIntOrNull()
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
        }
    }
}
