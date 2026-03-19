package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.util.scheduleDailyReminder
import com.dreamjournal.journalofdream.util.scheduleQuoteAlarms
import com.dreamjournal.journalofdream.util.canUseExactAlarms

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSetupScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val exactAlarmsAvailable = remember { canUseExactAlarms(context) }

    // TimeInput — компактный ввод ЧЧ:ММ, не занимает весь экран
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )

    // Пульсирующий glow как в онбординге
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
        BackgroundScreen()

        // Glow фон
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
                            colors = listOf(Color(0xFF7C4DFF), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7C4DFF).copy(alpha = 0.35f),
                                Color(0xFF7C4DFF).copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 44.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.setup_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.setup_subtitle),
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (!exactAlarmsAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.setup_exact_alarm_notice),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // TimeInput — компактный ввод ЧЧ:ММ без циферблата
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                )
            ) {
                TimeInput(
                    state = timePickerState,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = TimePickerDefaults.colors(
                        containerColor = Color.Transparent,
                        timeSelectorSelectedContainerColor = Color(0xFF7C4DFF),
                        timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.12f),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                        periodSelectorBorderColor = Color.White.copy(alpha = 0.3f),
                        periodSelectorSelectedContainerColor = Color(0xFF7C4DFF),
                        periodSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.08f),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                        clockDialColor = Color.Transparent,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                        selectorColor = Color(0xFF7C4DFF),
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Пропустить — сохраняем 8:00 по умолчанию
                        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putInt("notification_hour", 8)
                            .putInt("notification_minute", 0)
                            .putBoolean("notifications_enabled", true)
                            .putBoolean("motivational_quotes", true)
                            .apply()
                        scheduleDailyReminder(context, 8, 0)
                        scheduleQuoteAlarms(context)
                        onDone()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(stringResource(R.string.setup_skip), fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putInt("notification_hour", timePickerState.hour)
                            .putInt("notification_minute", timePickerState.minute)
                            .putBoolean("notifications_enabled", true)
                            .putBoolean("motivational_quotes", true)
                            .apply()
                        scheduleDailyReminder(context, timePickerState.hour, timePickerState.minute)
                        scheduleQuoteAlarms(context)
                        onDone()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text(stringResource(R.string.setup_done), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
