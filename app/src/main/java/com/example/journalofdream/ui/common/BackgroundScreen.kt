package com.example.journalofdream.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.journalofdream.R


// Функция для отображения фонового изображения
@Composable
fun BackgroundScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fon2), // Замените на ваше изображение
            contentDescription = null,
            contentScale = ContentScale.Crop, // Масштабирование изображения для заполнения экрана
            modifier = Modifier.fillMaxSize() // Заполнение всей области экрана
        )
    }
}