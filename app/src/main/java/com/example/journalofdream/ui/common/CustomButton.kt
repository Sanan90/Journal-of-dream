package com.example.journalofdream.ui.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 100)
    )

    Button(
        onClick = {
            scale = 0.95f  // Анимация сжатия при клике
            onClick()
            scale = 1f  // Вернуть масштаб
        },
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .scale(animatedScale)  // Анимация масштаба
            .shadow(12.dp, shape = RoundedCornerShape(20.dp))  // Глубокая тень для премиум-вида
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFBB86FC), Color(0xFF6200EE))  // Градиент purple
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Text(text = text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}