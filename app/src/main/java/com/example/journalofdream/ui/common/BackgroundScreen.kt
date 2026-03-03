package com.example.journalofdream.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.journalofdream.ui.theme.DeepNavy
import com.example.journalofdream.ui.theme.DeepPurple
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.journalofdream.R

// Функция для отображения фонового изображения
@Composable
fun BackgroundScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Градиент для премиум-вида (тёмно-синий в фиолетовый, как ночное небо)
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepNavy, DeepPurple)
                )
            ))
        // Если хочешь сохранить изображение как overlay, добавь его поверх градиента
        Image(
            painter = painterResource(id = R.drawable.fon2), // Замените на ваше изображение
            contentDescription = null,
            contentScale = ContentScale.Crop, // Масштабирование изображения для заполнения экрана
            modifier = Modifier.fillMaxSize().alpha(0.3f) // Полупрозрачный для смешивания с градиентом
        )
    }
}