// Файл: com/example/journalofdream/ui/auth/AuthScreen.kt

package com.example.journalofdream.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.journalofdream.R
import com.example.journalofdream.viewmodel.DreamViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Экран авторизации:
 * - Регистрация (email)
 * - Вход (email)
 * - Вход как гость
 * - Вход через Google
 *
 * После успешного входа выполняется DreamViewModel.onUserLogin() для миграции и синхронизации данных
 * и вызывается колбэк onAuthSuccess() для перехода на главный экран.
 */
@Composable
fun AuthScreen(
    dreamViewModel: DreamViewModel,
    onAuthSuccess: () -> Unit,
    onSkipAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    // Используем переданный DreamViewModel (общий для гостя и авторизованного пользователя)
    // val dreamViewModel: DreamViewModel = viewModel()  // (удалено: получаем ViewModel извне)

    // Поля состояния для ввода email и пароля
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Состояние для отображения сообщения об ошибке
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер активности для входа через Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { authResult ->
                    if (authResult.isSuccessful) {
                        // Успешный вход через Google -> FirebaseAuth
                        dreamViewModel.onUserLogin()  // переносим локальные записи guest в учетную запись и запускаем синхронизацию
                        onAuthSuccess()               // переходим на главный экран приложения
                    } else {
                        errorMessage = authResult.exception?.message
                    }
                }
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Поле ввода e-mail
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Электронная почта") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Поле ввода пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка регистрации нового аккаунта
            Button(
                onClick = {
                    keyboardController?.hide()
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Успешно зарегистрирован новый пользователь
                                dreamViewModel.onUserLogin()  // миграция данных гостя в аккаунт + запуск синхронизации
                                onAuthSuccess()               // переход на главный экран (MainScreen)
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Регистрация")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка входа в существующий аккаунт
            Button(
                onClick = {
                    keyboardController?.hide()
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Успешный вход в аккаунт пользователя
                                dreamViewModel.onUserLogin()  // перенос локальных записей (guest) в аккаунт пользователя
                                onAuthSuccess()               // переход на главный экран
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка входа как гость
            Button(
                onClick = {
                    // Гостевой режим: остаемся без авторизации
                    // (DreamViewModel по-прежнему использует "guest" для ownerUid)
                    onSkipAuth()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти как гость")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка входа через Google
            Button(
                onClick = {
                    // Запускаем стандартный intent Google Sign-In
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти через Google")
            }
        }
    }

    // Если появилось сообщение об ошибке – показываем Snackbar внизу экрана
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }
}
