// Файл: com/example/journalofdream/ui/auth/AuthScreen.kt

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
import com.example.journalofdream.R
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSkipAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    // Переменные состояния для ввода электронной почты и пароля
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Переменная состояния для отображения ошибки
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    val keyboardController = LocalSoftwareKeyboardController.current

    // Состояние для Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Лаунчер для активности Google Sign-In
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
                        // Успешная авторизация
                        onAuthSuccess()
                    } else {
                        // Ошибка авторизации
                        errorMessage = authResult.exception?.message
                    }
                }
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Поле ввода электронной почты
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
                        // Настройка Google Sign-In
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
    )

    // Отображение ошибки в Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }
}
