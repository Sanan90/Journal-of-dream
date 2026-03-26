package com.dreamjournal.journalofdream

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlin.math.sin

class SplashActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

        setContent {
            SplashScreenContent(
                onFinished = {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            )
        }
    }
}

@Composable
fun SplashScreenContent(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
        delay(180)
        onFinished()
    }

    val p = progress.value

    val lightBgAlpha = ((p - 0.34f) / 0.42f).coerceIn(0f, 1f)

    val moonMoveP = ((p - 0.08f) / 0.62f).coerceIn(0f, 1f)
    val moonEased = FastOutSlowInEasing.transform(moonMoveP)
    val moonAlpha = (1f - ((p - 0.62f) / 0.16f)).coerceIn(0f, 1f)

    val sunMoveP = ((p - 0.28f) / 0.50f).coerceIn(0f, 1f)
    val sunEased = FastOutSlowInEasing.transform(sunMoveP)
    val sunAlpha = ((p - 0.28f) / 0.20f).coerceIn(0f, 1f)

    val titleProgress = ((p - 0.40f) / 0.26f).coerceIn(0f, 1f)
    val titleEased = FastOutSlowInEasing.transform(titleProgress)
    val titleAlpha = titleProgress
    val titleScale = 0.86f + 0.14f * titleEased

    val gabrielaFamily = FontFamily(
        Font(R.font.gabriela_regular)
    )

    val titleBrush = ShaderBrush(
        ImageShader(
            ImageBitmap.imageResource(R.drawable.dream_text_gradient),
            TileMode.Clamp,
            TileMode.Clamp
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.dark_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(R.drawable.light_splash),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(lightBgAlpha),
            contentScale = ContentScale.Crop
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()

            val bodyPx = screenW * 0.8f
            val bodyDp = with(density) { bodyPx.toDp() }

            val centerX = (screenW - bodyPx) / 2f
            val centerY = screenH * 0.75f - bodyPx / 2f

            val moonStartX = centerX
            val moonEndX = screenW + bodyPx * 0.35f
            val moonBaseY = centerY

            val sunStartX = -bodyPx * 1.15f
            val sunEndX = centerX
            val sunBaseY = centerY

            val archHeight = screenH * 0.12f
            val moonArcY = -4f * archHeight * moonEased * (1f - moonEased)
            val sunArcY = -4f * archHeight * sunEased * (1f - sunEased)

            val moonX = moonStartX + (moonEndX - moonStartX) * moonEased
            val moonY = moonBaseY + moonArcY

            val sunX = sunStartX + (sunEndX - sunStartX) * sunEased
            val sunY = sunBaseY + sunArcY

            val moonXdp = with(density) { moonX.toDp() }
            val moonYdp = with(density) { moonY.toDp() }
            val sunXdp = with(density) { sunX.toDp() }
            val sunYdp = with(density) { sunY.toDp() }

            val titleTargetY = screenH * 0.14f
            val titleStartY = -screenH * 0.18f
            val bounce = if (titleProgress > 0.70f) {
                sin((titleProgress - 0.70f) / 0.30f * Math.PI).toFloat() * 22f
            } else {
                0f
            }
            val titleY = titleStartY + (titleTargetY - titleStartY) * titleEased + bounce
            val titleYDp = with(density) { titleY.toDp() }

            Text(
                text = stringResource(R.string.app_name),
                style = TextStyle(
                    fontFamily = gabrielaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 44.sp,
                    brush = titleBrush,
                    shadow = Shadow(
                        color = androidx.compose.ui.graphics.Color(0x55000000),
                        offset = Offset(0f, 4f),
                        blurRadius = 14f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .absoluteOffset(y = titleYDp)
                    .alpha(titleAlpha)
                    .scale(titleScale)
            )

            Image(
                painter = painterResource(R.drawable.moon_splash),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(bodyDp)
                    .absoluteOffset(x = moonXdp, y = moonYdp)
                    .alpha(moonAlpha)
            )

            Image(
                painter = painterResource(R.drawable.sun_splash),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(bodyDp)
                    .absoluteOffset(x = sunXdp, y = sunYdp)
                    .alpha(sunAlpha)
            )
        }
    }
}
