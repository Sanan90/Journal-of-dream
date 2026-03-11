package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    val colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        PremiumDarkColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography2,
        content = content
    )
}
