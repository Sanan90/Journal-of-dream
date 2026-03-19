package com.dreamjournal.journalofdream.ui

import com.dreamjournal.journalofdream.ui.auth.removePin
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dreamjournal.journalofdream.ui.dreams.*
import com.dreamjournal.journalofdream.ui.locations.*
import com.dreamjournal.journalofdream.ui.characters.*
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreamjournal.journalofdream.ui.auth.AuthScreen
import com.dreamjournal.journalofdream.ui.auth.PinMode
import com.dreamjournal.journalofdream.ui.auth.PinScreen
import com.dreamjournal.journalofdream.ui.auth.hasPin
import com.dreamjournal.journalofdream.ui.auth.isPinEnabled
import com.dreamjournal.journalofdream.ui.auth.syncPinFromFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.theme.DreamsScreen
import com.dreamjournal.journalofdream.ui.theme.LocationListScreen
import com.dreamjournal.journalofdream.ui.theme.CharacterListScreen
import com.dreamjournal.journalofdream.ui.theme.MainScreen
import com.dreamjournal.journalofdream.ui.theme.AchievementsScreen
import com.dreamjournal.journalofdream.ui.theme.ChartScreen
import com.dreamjournal.journalofdream.ui.theme.SettingsScreen
import com.dreamjournal.journalofdream.ui.theme.StatsScreen
import com.dreamjournal.journalofdream.ui.theme.TechniquesScreen
import com.dreamjournal.journalofdream.ui.theme.QuotesAdminScreen
import com.dreamjournal.journalofdream.ui.theme.OnboardingScreen
import com.dreamjournal.journalofdream.ui.theme.SupportScreen
import com.dreamjournal.journalofdream.ui.theme.NotificationSetupScreen
import com.dreamjournal.journalofdream.viewmodel.TechniqueViewModel
import com.dreamjournal.journalofdream.ui.theme.ViewDreamScreen
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.launch

/**
 * Главный composable приложения, где определяются NavHost и навигация между экранами.
 * Определяет, показывать ли экран авторизации или основной контент, в зависимости от состояния (guest или авторизован).
 */
@Composable
fun JournalOfDreamApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val currentUser = remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
        val skipAuth = remember { mutableStateOf(sharedPreferences.getBoolean("skipAuth", false)) }

        val requiresPin = (currentUser.value != null || skipAuth.value) && isPinEnabled(context)
        var pinUnlocked by remember(currentUser.value?.uid, skipAuth.value, requiresPin) {
            mutableStateOf(!requiresPin)
        }

        if (requiresPin && !pinUnlocked) {
            PinScreen(
                mode = PinMode.ENTER,
                onSuccess = { pinUnlocked = true },
                onForgotPin = {
                    removePin(context)
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    sharedPreferences.edit().putBoolean("skipAuth", false).apply()
                    (context as? android.app.Activity)?.recreate()
                }
            )
            return
        }


        // Онбординг — показываем один раз при первом запуске
        var onboardingDone by remember {
            mutableStateOf(sharedPreferences.getBoolean("onboarding_done", false))
        }
        if (!onboardingDone) {
            OnboardingScreen(
                onFinish = {
                    sharedPreferences.edit().putBoolean("onboarding_done", true).apply()
                    onboardingDone = true
                }
            )
            return
        }

        // Настройка уведомления — показываем один раз после онбординга
        var notifSetupDone by remember {
            mutableStateOf(sharedPreferences.contains("notification_hour"))
        }
        if (!notifSetupDone) {
            NotificationSetupScreen(
                onDone = {
                    notifSetupDone = true
                }
            )
            return
        }

        // NavController для навигации по экранам
        val navController = rememberNavController()

        // Инициализируем ViewModel-ы (живут на уровне Activity)
        val dreamViewModel: DreamViewModel = viewModel()
        val locationViewModel: LocationViewModel = viewModel()
        val characterViewModel: CharacterViewModel = viewModel()
        val categoryViewModel: CategoryViewModel = viewModel()
        val techniqueViewModel: TechniqueViewModel = viewModel()

        // Режим админа — общий для Техник и Настроек
        var isAdminMode by remember { mutableStateOf(false) }

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

        // Для отображения имени пользователя (в заголовке MainScreen)
        // displayName в приоритете — берём часть до @ если только email
        val userName = currentUser.value?.let { user ->
            val name = user.displayName
            if (!name.isNullOrBlank()) name
            else user.email?.substringBefore("@")
        }

        // Определяем стартовый экран: если пользователь уже авторизован или выбрал guest-режим, идём на main, иначе на auth
        NavHost(
            navController = navController,
            startDestination = if (currentUser.value != null || isGuest) "main" else "auth",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // Экран авторизации
            composable("auth") {
                AuthScreen(
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel,
                    characterViewModel = characterViewModel,
                    onAuthSuccess = {
                        currentUser.value = auth.currentUser
                        skipAuth.value = false
                        sharedPreferences.edit().putBoolean("skipAuth", false).apply()
                        auth.currentUser?.uid?.let { uid ->
                            categoryViewModel.setOwner(uid)
                            scope.launch { syncPinFromFirestore(context) }
                        }
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
                            characterViewModel.onUserLogout()
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

            // Экран просмотра сна
            composable("viewDream/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                ViewDreamScreen(
                    navController = navController,
                    dreamId = id,
                    dreamViewModel = dreamViewModel
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

            composable("characters") {
                CharacterListScreen(
                    navController = navController,
                    characterViewModel = characterViewModel
                )
            }

            composable("addCharacter") {
                AddCharacterScreen(
                    navController = navController,
                    characterViewModel = characterViewModel
                )
            }

            composable("viewCharacter/{characterId}") { backStackEntry ->
                val characterId = backStackEntry.arguments?.getString("characterId")?.toIntOrNull()
                if (characterId != null) {
                    ViewCharacterScreen(
                        navController = navController,
                        characterId = characterId,
                        characterViewModel = characterViewModel
                    )
                }
            }

            // Экран техник осознанных сновидений
            composable("techniques") {
                TechniquesScreen(
                    navController = navController,
                    isAdminMode = isAdminMode,
                    onAdminModeChanged = { isAdminMode = it }
                )
            }

            // Экран достижений
            composable("achievements") {
                AchievementsScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel
                )
            }

            // Экран графика активности
            composable("chart") {
                ChartScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel
                )
            }

            // Экран настроек
            composable("settings") {
                SettingsScreen(navController = navController, isAdminMode = isAdminMode)
            }

            // Экран управления цитатами (только для админа)
            composable("quotes_admin") {
                QuotesAdminScreen(navController = navController)
            }

            // Экран статистики
            composable("support") {
                SupportScreen(navController = navController)
            }
            composable("stats") {
                StatsScreen(
                    navController = navController,
                    dreamViewModel = dreamViewModel,
                    locationViewModel = locationViewModel,
                    characterViewModel = characterViewModel
                )
            }
        }
}
