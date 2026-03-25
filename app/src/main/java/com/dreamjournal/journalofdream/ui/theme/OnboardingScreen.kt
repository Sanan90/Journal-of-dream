package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.border

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val body: String,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    // Тексты берутся из strings.xml — автоматически на языке приложения
    val pages = listOf(
        OnboardingPage(
            emoji = "🌙",
            title = stringResource(R.string.onboarding_1_title),
            body = stringResource(R.string.onboarding_1_body),
            accentColor = Color(0xFF7C4DFF)
        ),
        OnboardingPage(
            emoji = "📖",
            title = stringResource(R.string.onboarding_2_title),
            body = stringResource(R.string.onboarding_2_body),
            accentColor = Color(0xFF42A5F5)
        ),
        OnboardingPage(
            emoji = "🗺️",
            title = stringResource(R.string.onboarding_3_title),
            body = stringResource(R.string.onboarding_3_body),
            accentColor = Color(0xFF26C6DA)
        ),
        OnboardingPage(
            emoji = "✨",
            title = stringResource(R.string.onboarding_4_title),
            body = stringResource(R.string.onboarding_4_body),
            accentColor = Color(0xFF66BB6A)
        )
    )

    var currentPage by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Glow-круг за эмодзи
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 80.dp)
                    .size(240.dp)
                    .scale(glowScale)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(pages[currentPage].accentColor, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "page"
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Эмодзи в кружке
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        pages[page].accentColor.copy(alpha = 0.3f),
                                        pages[page].accentColor.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = pages[page].emoji, fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = pages[page].title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0D68C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Карточка с текстом (прокручивается если не помещается)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 280.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF3B1A58).copy(alpha = 0.72f), Color(0xFF1A0C30).copy(alpha = 0.88f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                Color(0xFFF0D68C).copy(alpha = 0.20f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = pages[page].body,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 23.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Точки-индикаторы
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val isActive = index == currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isActive) pages[currentPage].accentColor
                                else Color.White.copy(alpha = 0.3f)
                            )
                            .clickable { currentPage = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage == 0) {
                    TextButton(onClick = onFinish) {
                        Text(
                            stringResource(R.string.onboarding_skip),
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 15.sp
                        )
                    }
                } else {
                    TextButton(onClick = { currentPage-- }) {
                        Text(
                            stringResource(R.string.onboarding_back),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) currentPage++
                        else onFinish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B3FA0)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (currentPage < pages.size - 1)
                            stringResource(R.string.onboarding_next)
                        else
                            stringResource(R.string.onboarding_start),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFFF0D68C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
