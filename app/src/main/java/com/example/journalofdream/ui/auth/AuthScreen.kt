package com.example.journalofdream.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.journalofdream.R
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit, onSkipAuth: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()

    var isLoading by remember { mutableStateOf(false) }

    // Лаунчер для результата активности Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { authResult ->
                    isLoading = false
                    if (authResult.isSuccessful) {
                        // Успешная авторизация
                        onAuthSuccess()
                    } else {
                        // Ошибка авторизации
                        // Обработайте ошибку (например, покажите сообщение)
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            e.printStackTrace()
            // Обработайте ошибку (например, покажите сообщение)
        }
    }

    // UI для экрана авторизации
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                // Показать индикатор загрузки
                Text(text = "Загрузка...")
            } else {
                Button(onClick = {
                    isLoading = true
                    // Настройка параметров Google Sign-In
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }) {
                    Text(text = "Войти с помощью Google")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Переход без авторизации
                    onSkipAuth()
                }) {
                    Text(text = "Войти без авторизации")
                }
            }
        }
    }
}
