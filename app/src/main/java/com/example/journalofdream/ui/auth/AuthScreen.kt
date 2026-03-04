package com.example.journalofdream.ui.auth

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.journalofdream.R
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current

    // Валидация полей
    fun validateInputs(): Boolean {
        var valid = true
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Введите корректный email"
            valid = false
        } else {
            emailError = null
        }
        if (password.length < 6) {
            passwordError = "Пароль должен содержать минимум 6 символов"
            valid = false
        } else {
            passwordError = null
        }
        return valid
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { authResult ->
                    isLoading = false
                    if (authResult.isSuccessful) {
                        auth.currentUser?.let { user ->
                            locationViewModel.onUserLogin(user)
                            dreamViewModel.onUserLogin()
                        }
                        onAuthSuccess()
                    } else {
                        errorMessage = authResult.exception?.message
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Добро пожаловать",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
            )

            // Поле Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    errorMessage = null
                },
                label = { Text("Электронная почта") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Поле пароля
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    errorMessage = null
                },
                label = { Text("Пароль") },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = if (passwordVisible)
                                "Скрыть пароль"
                            else
                                "Показать пароль"
                        )
                    }
                },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Забыл пароль"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            emailError = "Введите корректный email для сброса пароля"
                        } else {
                            auth.sendPasswordResetEmail(email.trim())
                                .addOnCompleteListener { task ->
                                    resetMessage = if (task.isSuccessful) {
                                        "Письмо для сброса пароля отправлено на ${email.trim()}"
                                    } else {
                                        "Ошибка: ${task.exception?.message}"
                                    }
                                }
                        }
                    }
                ) {
                    Text("Забыл пароль?", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Сообщение об отправке письма
            resetMessage?.let { msg ->
                Text(
                    text = msg,
                    color = if (msg.startsWith("Ошибка"))
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Войти"
            Button(
                onClick = {
                    if (!validateInputs()) return@Button
                    keyboardController?.hide()
                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener(activity!!) { authResult ->
                            isLoading = false
                            if (authResult.isSuccessful) {
                                auth.currentUser?.let { user ->
                                    locationViewModel.onUserLogin(user)
                                    dreamViewModel.onUserLogin()
                                }
                                onAuthSuccess()
                            } else {
                                errorMessage = "Неверный email или пароль"
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Войти")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Регистрация"
            OutlinedButton(
                onClick = {
                    if (!validateInputs()) return@OutlinedButton
                    keyboardController?.hide()
                    isLoading = true
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener(activity!!) { authResult ->
                            isLoading = false
                            if (authResult.isSuccessful) {
                                auth.currentUser?.let { user ->
                                    locationViewModel.onUserLogin(user)
                                    dreamViewModel.onUserLogin()
                                }
                                onAuthSuccess()
                            } else {
                                errorMessage = authResult.exception?.message
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрироваться")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка входа через Google
            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти через Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Сообщение об ошибке от Firebase
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Гостевой режим
            TextButton(
                onClick = { onSkipAuth() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Продолжить без авторизации")
            }
        }

        // Полноэкранный оверлей загрузки поверх всего экрана
        if (isLoading) {
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Выполняется вход...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        } // конец внешнего Box
    }
}
