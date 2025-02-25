package com.example.journalofdream.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.R
import com.example.journalofdream.viewmodel.DreamViewModel
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Экран авторизации:
 * - Регистрация через e-mail
 * - Вход через e-mail
 * - Вход как гость
 * - Вход через Google
 *
 * После успешной авторизации вызывает onAuthSuccess() и DreamViewModel.onUserLogin().
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSkipAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    // В AuthScreen получаем DreamViewModel (допустим, через viewModel())
    val dreamViewModel: DreamViewModel = viewModel()

    // Поля ввода
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Для вывода ошибок
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер для Google Sign-In
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
                        // Успешная авторизация Google->Firebase
                        // Вызываем onUserLogin в VM, чтобы перенести записи из guest и стартовать синх.
                        dreamViewModel.onUserLogin()
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

            // Кнопка регистрации
            Button(
                onClick = {
                    keyboardController?.hide()
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Новый пользователь
                                dreamViewModel.onUserLogin() // перенести guest->новый аккаунт
                                onAuthSuccess()
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

            // Кнопка входа
            Button(
                onClick = {
                    keyboardController?.hide()
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Успешный вход
                                dreamViewModel.onUserLogin()
                                onAuthSuccess()
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

    // Отображение ошибки в Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }
}
