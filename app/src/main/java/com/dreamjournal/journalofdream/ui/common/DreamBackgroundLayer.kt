package com.dreamjournal.journalofdream.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.dreamjournal.journalofdream.R

/**
 * Универсальный слой фона поверх основного фона приложения.
 * Поддерживает два типа:
 *   - Фото-фоны (ID >= 200) — рендерятся как Image с лёгким затемнением
 *   - Градиентные фоны (ID 1-15) — рендерятся как Box с Brush
 *   - ID 0 — ничего не отображается
 *
 * Используется на экранах снов, локаций и образов.
 */
@Composable
fun DreamBackgroundLayer(backgroundId: Int) {
    if (backgroundId == 0) return
    if (DreamBackgrounds.isPhoto(backgroundId)) {
        val resId = photoResId(backgroundId) ?: return
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.70f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )
        }
    } else {
        val brush = DreamBackgrounds.getBrush(backgroundId) ?: return
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .background(Color.Black.copy(alpha = 0.35f))
        )
    }
}
