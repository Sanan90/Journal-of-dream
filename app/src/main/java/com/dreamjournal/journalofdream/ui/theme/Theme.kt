package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = Gold,
    background = DarkSurface,
    surface = CardSurface,
    onPrimary = Color.Black,
    onBackground = Color.White
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    // FIX: всегда используем фирменную тёмно-фиолетовую тему.
    // Убрана dynamicDarkColorScheme — она брала цвета с обоев телефона
    // и ломала золото-фиолетовый дизайн на Android 12+.
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = Typography2,
        content = content
    )
}
