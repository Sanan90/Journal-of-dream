package com.example.journalofdream.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Файл для настройки Material 3 (светлой/тёмной темы).
 */

// 1) Определим Light-схему (светлую)
private val LightColors = lightColorScheme(
    // Эти цвета можете заменить на любые свои.
    // Пример: для primary/secondary/background вы можете выбрать любые hex-значения
    primary = Purple40,
    onPrimary = White,
    secondary = PurpleGrey40,
    onSecondary = White,
    background = Grey99,
    onBackground = Grey10,
    // etc...
)

// 2) Определим Dark-схему (тёмную)
private val DarkColors = darkColorScheme(
    primary = Purple80,
    onPrimary = Grey20,
    secondary = PurpleGrey80,
    onSecondary = Grey20,
    background = Grey10,
    onBackground = Grey90,
    // etc...
)

/**
 * Основная тема приложения.
 *
 * @param darkTheme Если true, насильно включаем тёмную тему.
 *                  По умолчанию берём системную настройку через [isSystemInDarkTheme].
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Выбираем, какие цвета использовать
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        // Если у вас есть собственные Fonts / Shapes / Typography - подключите их тоже
        typography = Typography,
        shapes = MyShapes,
        content = content
    )
}
