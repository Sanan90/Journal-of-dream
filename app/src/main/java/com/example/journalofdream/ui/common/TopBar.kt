package com.example.journalofdream.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun TopBar(navController: NavHostController, onSaveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp) // Увеличиваем верхний отступ
            .height(60.dp), // Задаём высоту для большей площади
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(48.dp) // Увеличиваем размер иконки
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White, modifier = Modifier.size(36.dp)) // Иконка увеличена до 36dp
        }

        IconButton(
            onClick = onSaveClick,
            modifier = Modifier.size(48.dp) // Увеличиваем размер иконки
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Сохранить", tint = Color.White, modifier = Modifier.size(36.dp)) // Иконка увеличена до 36dp
        }
    }
}