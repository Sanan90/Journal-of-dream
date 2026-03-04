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
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDate(date: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(date)!!)
    } catch (e: Exception) {
        date
    }
}

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
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formatDate(dream.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (dream.content.isNotBlank()) {
                Text(
                    text = if (dream.content.length > 100)
                        dream.content.take(100) + "..."
                    else
                        dream.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (dream.category.isNotBlank() && dream.category != "Без категории") {
                Text(
                    text = dream.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}