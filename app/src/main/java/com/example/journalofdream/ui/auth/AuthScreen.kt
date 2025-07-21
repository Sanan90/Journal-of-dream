package com.example.journalofdream.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.R
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Экран авторизации (Email/Пароль + Google).
 * После успешного входа вызываем:
 *   locationViewModel.onUserLogin(user)
 *   dreamViewModel.onUserLogin()
 * чтобы мигрировать данные гостя и начать синхронизацию локальных списков локаций/снов.
 */
@Composable
fun AuthScreen(
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    onAuthSuccess: () -> Unit,
    onSkipAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер для результата входа через Google
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
                        val user = auth.currentUser
                        if (user != null) {
                            // После успешного входа вызываем переключение ViewModel на этого пользователя
                            locationViewModel.onUserLogin(user)
                            dreamViewModel.onUserLogin()
                        }
                        // Переходим на основной экран
                        onAuthSuccess()
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
            // Поле ввода Email
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
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Кнопка "Войти"
            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity!!) { authResult ->
                            if (authResult.isSuccessful) {
                                // Успешный вход с email/password
                                val user = auth.currentUser
                                if (user != null) {
                                    locationViewModel.onUserLogin(user)
                                    dreamViewModel.onUserLogin()
                                }
                                onAuthSuccess()
                            } else {
                                errorMessage = authResult.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти")
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Кнопка "Регистрация"
            Button(
                onClick = {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity!!) { authResult ->
                            if (authResult.isSuccessful) {
                                // Успешная регистрация автоматически выполняет вход
                                val user = auth.currentUser
                                if (user != null) {
                                    locationViewModel.onUserLogin(user)
                                    dreamViewModel.onUserLogin()
                                }
                                onAuthSuccess()
                            } else {
                                errorMessage = authResult.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Регистрация")
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Кнопка входа через Google
            Button(
                onClick = {
                    // Запускаем Intent для Google Sign-In
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти через Google")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Кнопка пропустить авторизацию (гостевой режим)
            TextButton(onClick = { onSkipAuth() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Продолжить без авторизации")
            }

            // Отображение ошибки (если есть)
            errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
