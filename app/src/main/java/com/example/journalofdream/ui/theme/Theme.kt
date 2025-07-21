package com.example.journalofdream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val PremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),  // Neon-purple для акцентов
    secondary = Color(0xFFFFD700),  // Gold для премиум-элементов
    background = Color(0xFF121212),  // Тёмный фон
    surface = Color(0xFF1E1E1E),  // Карточки чуть светлее
    onPrimary = Color.Black,
    onBackground = Color.White
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)  // Динамические цвета от обоев
    } else {
        PremiumDarkColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography2,  // Ссылка на Typography.kt
        content = content
    )
}