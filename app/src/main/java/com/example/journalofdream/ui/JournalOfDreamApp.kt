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

        // Создаём NavController для навигации
        val navController = rememberNavController()

        // ViewModel
        val dreamViewModel: DreamViewModel = viewModel()
        val locationViewModel: LocationViewModel = viewModel()

        // Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val currentUser = remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

        // Гостевой режим (skipAuth)
        val skipAuth = remember { mutableStateOf(false) }

        // Читаем skipAuth из SharedPreferences при запуске приложения
        LaunchedEffect(Unit) {
            skipAuth.value = sharedPreferences.getBoolean("skipAuth", false)
        }

        // GoogleSignInClient (для выхода из Google, если нужно)
        val googleSignInClient: GoogleSignInClient = remember {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
        }


        // 1) Определяем, в каком режиме мы находимся: гость или авторизованный
        //    Если currentUser = null ИЛИ skipAuth = true => это гость.
        val isGuest = (currentUser.value == null || skipAuth.value)

        // 2) Узнаём имя пользователя (если авторизован).
        //    Может быть displayName, а может быть email,
        //    если пользователь зашёл через почту/пароль и не указал имя в FirebaseProfile.
        //    Если хотите просто имя, можете брать displayName. Или email, если displayName = null.
        val userName = currentUser.value?.displayName ?: currentUser.value?.email

        // Решаем, с какого экрана начать
        val startDestination = if (isGuest) "auth" else "main"


        // Запускаем NavHost
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // ЭКРАН АВТОРИЗАЦИИ
            composable("auth") {
                AuthScreen(
                    onAuthSuccess = {
                        // Когда удачно авторизовались
                        currentUser.value = auth.currentUser
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()

                        // Переходим на главный экран
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onSkipAuth = {
                        // Когда нажали "Войти как гость"
                        skipAuth.value = true
                        sharedPreferences.edit().putBoolean("skipAuth", true).apply()

                        // Переходим на главный экран
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Главный экран
            composable("main") {
                MainScreen(
                    navController = navController,
                    onLogout = {
                        // Выход из FirebaseAuth, если пользователь авторизован
                        if (auth.currentUser != null) {
                            auth.signOut()
                        }
                        // Выход из GoogleSignInClient (Google)
                        googleSignInClient.signOut()

                        // Обнуляем currentUser
                        currentUser.value = null

                        // Сброс гостевого режима
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()

                        // Переходим на экран авторизации
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    // Добавляем параметры для отображения в top bar
                    isGuest = isGuest,         // гость или нет
                    displayName = userName     // имя или e-mail
                )
            }

            // Остальные экраны (пример: список снов)
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

            // Экраны для локаций
            composable("locations") {
                LocationListScreen(navController, locationViewModel)
            }

            composable("addLocation") {
                AddLocationScreen(navController, locationViewModel)
            }

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
        }
    }
}




