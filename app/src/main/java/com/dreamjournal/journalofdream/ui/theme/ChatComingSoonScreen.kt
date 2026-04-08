package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark  = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

@Composable
fun ChatComingSoonScreen(navController: NavHostController) {

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Фон
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // Верхняя панель
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint = GoldLight,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.nav_chat),
                    color = GoldLight,
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }

            // Контент по центру
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // Иконка с пульсирующим свечением
                    Box(contentAlignment = Alignment.Center) {
                        // Свечение
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .alpha(pulseAlpha)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF9C27B0).copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        // Карточка с эмодзи
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .shadow(12.dp, RoundedCornerShape(28.dp))
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF3B1A58).copy(alpha = 0.90f),
                                            Color(0xFF1A0C30).copy(alpha = 0.95f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFF0D68C).copy(alpha = 0.4f),
                                            Color(0xFFF0D68C).copy(alpha = 0.1f)
                                        )
                                    ),
                                    RoundedCornerShape(28.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 44.sp)
                        }
                    }

                    // Заголовок
                    Text(
                        text = stringResource(R.string.chat_coming_soon_title),
                        color = GoldLight,
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )

                    // Описание
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF3B1A58).copy(alpha = 0.72f),
                                        Color(0xFF1A0C30).copy(alpha = 0.82f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Color(0xFFF0D68C).copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.chat_coming_soon_message),
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Декоративные звёзды
                    Text(
                        text = "✦  ✦  ✦",
                        color = GoldDark.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        letterSpacing = 8.sp
                    )
                }
            }
        }
    }
}
