package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.R

private val GoldLightO = Color(0xFFF0D68C)
private val CardBgO = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.75f), Color(0xFF1A0C30).copy(alpha = 0.90f))
)
private val PlayfairO = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

data class OnboardingPageData(
    val iconRes: Int,
    val accentColor: Color,
    val titleRes: Int,
    val bodyRes: Int
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    val pages = listOf(
        // 1. Что такое осознанные сны
        OnboardingPageData(R.drawable.elegant_golden_moon,  Color(0xFF7C4DFF), R.string.onboarding_1_title, R.string.onboarding_1_body),
        // 2. Зачем записывать сны
        OnboardingPageData(R.drawable.dream_journal_icon,   Color(0xFF42A5F5), R.string.onboarding_2_title, R.string.onboarding_2_body),
        // 3. Локации — карта снов
        OnboardingPageData(R.drawable.location_icon_gold,   Color(0xFF26C6DA), R.string.onboarding_3_title, R.string.onboarding_3_body),
        // 4. Образы сна — персонажи
        OnboardingPageData(R.drawable.dreamers_icon,        Color(0xFFEC407A), R.string.onboarding_5_title, R.string.onboarding_5_body),
        // 5. Техники осознанных снов
        OnboardingPageData(R.drawable.magic_book_icon,      Color(0xFFFF7043), R.string.onboarding_6_title, R.string.onboarding_6_body),
        // 6. Готов начать
        OnboardingPageData(R.drawable.graphic_cosmo_icon,   Color(0xFF66BB6A), R.string.onboarding_4_title, R.string.onboarding_4_body)
    )

    var currentPage by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        0.85f, 1.15f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "gs"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.12f, 0.38f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "ga"
    )

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Свечение
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .padding(top = 70.dp).size(260.dp)
                    .scale(glowScale).alpha(glowAlpha)
                    .background(
                        Brush.radialGradient(listOf(pages[currentPage].accentColor, Color.Transparent)),
                        CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    else
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                },
                label = "page"
            ) { page ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

                    // Иконка
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.radialGradient(listOf(pages[page].accentColor.copy(0.35f), pages[page].accentColor.copy(0.05f))),
                                CircleShape
                            )
                            .border(1.dp, pages[page].accentColor.copy(0.30f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(pages[page].iconRes), null,
                            Modifier.size(64.dp), contentScale = ContentScale.Fit)
                    }

                    Spacer(Modifier.height(22.dp))

                    // Заголовок
                    Text(
                        stringResource(pages[page].titleRes),
                        fontFamily = PlayfairO, fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, color = GoldLightO,
                        textAlign = TextAlign.Center, lineHeight = 30.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Карточка
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 130.dp, max = 260.dp)
                            .background(CardBgO, RoundedCornerShape(18.dp))
                            .border(1.dp, GoldLightO.copy(0.18f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            stringResource(pages[page].bodyRes),
                            fontSize = 15.sp, color = Color.White.copy(0.88f),
                            lineHeight = 23.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Индикатор
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                pages.indices.forEach { index ->
                    val isActive = index == currentPage
                    val dotWidth by animateDpAsState(if (isActive) 28.dp else 7.dp, tween(300), label = "dot")
                    Box(
                        modifier = Modifier.height(7.dp).width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) pages[currentPage].accentColor else Color.White.copy(0.28f))
                            .clickable { currentPage = index }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Кнопки навигации
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (currentPage == 0) {
                    TextButton(onClick = onFinish) {
                        Text(stringResource(R.string.onboarding_skip), color = Color.White.copy(0.40f), fontSize = 15.sp)
                    }
                } else {
                    TextButton(onClick = { currentPage-- }) {
                        Text(stringResource(R.string.onboarding_back), color = Color.White.copy(0.55f), fontSize = 15.sp)
                    }
                }

                Text("${currentPage + 1} / ${pages.size}", color = GoldLightO.copy(0.55f), fontSize = 13.sp)

                Button(
                    onClick = { if (currentPage < pages.size - 1) currentPage++ else onFinish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B3FA0)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(46.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldLightO.copy(0.40f))
                ) {
                    Text(
                        if (currentPage < pages.size - 1) stringResource(R.string.onboarding_next)
                        else stringResource(R.string.onboarding_start),
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GoldLightO
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
