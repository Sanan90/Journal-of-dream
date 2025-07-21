package com.example.journalofdream.ui.dreams

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.journalofdream.model.Dream

@Composable
fun DreamListItem(dream: Dream, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("editDream/${dream.localId}") }
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),  // Мягкая анимация расширения
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),  // Тень для глубины
        shape = RoundedCornerShape(16.dp),  // Скруглённые углы
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)  // Тёмная поверхность
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dream.title,
                style = MaterialTheme.typography.headlineMedium,  // Элегантный шрифт
                color = MaterialTheme.colorScheme.primary  // Purple акцент
            )
            Text(
                text = dream.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Если есть описание, добавь: Text(dream.content.take(100) + "...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}