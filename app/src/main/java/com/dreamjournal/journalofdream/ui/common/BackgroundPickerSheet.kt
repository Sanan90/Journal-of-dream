package com.dreamjournal.journalofdream.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.R

private val GoldLight = Color(0xFFF0D68C)

// ─── Маппинг ID → drawable resource ─────────────────────────────────────────

fun photoResId(id: Int): Int? = when {
    id in 201..215 -> dreamPhotoRes(id - 200)
    id in 301..316 -> locationPhotoRes(id - 300)
    id in 401..412 -> characterPhotoRes(id - 400)
    else -> null
}

private fun dreamPhotoRes(n: Int): Int = when (n) {
    1  -> R.drawable.dreamfon1;  2  -> R.drawable.dreamfon2
    3  -> R.drawable.dreamfone3;  4  -> R.drawable.dreamfon4
    5  -> R.drawable.dreamfone5;  6  -> R.drawable.dreamfone6
    7  -> R.drawable.dreamfone7;  8  -> R.drawable.dreamfone8
    9  -> R.drawable.dreamfone9;  10 -> R.drawable.dreamfone10
    11 -> R.drawable.dreamfone11; 12 -> R.drawable.dreamfone12
    13 -> R.drawable.dreamfone13; 14 -> R.drawable.dreamfone14
    15 -> R.drawable.dreamfone15
    else -> error("Неизвестный номер dreamfon: $n")
}

private fun locationPhotoRes(n: Int): Int = when (n) {
    1  -> R.drawable.locationfone1;  2  -> R.drawable.locationfone2
    3  -> R.drawable.locationfone3;  4  -> R.drawable.locationfone4
    5  -> R.drawable.locationfone5;  6  -> R.drawable.locationfone6
    7  -> R.drawable.locationfone7;  8  -> R.drawable.locationfone8
    9  -> R.drawable.locationfone9;  10 -> R.drawable.locationfone10
    11 -> R.drawable.locationfone11; 12 -> R.drawable.locationfone12
    13 -> R.drawable.locationfone13; 14 -> R.drawable.locationfone14
    15 -> R.drawable.locationfone15; 16 -> R.drawable.locationfone16
    else -> error("Неизвестный номер locationfone: $n")
}

private fun characterPhotoRes(n: Int): Int = when (n) {
    1  -> R.drawable.characterfone1;  2  -> R.drawable.characterfone2
    3  -> R.drawable.characterfone3;  4  -> R.drawable.characterfone4
    5  -> R.drawable.characterfone5;  6  -> R.drawable.characterfone6
    7  -> R.drawable.characterfone7;  8  -> R.drawable.characterfone8
    9  -> R.drawable.characterfone9;  10 -> R.drawable.characterfone10
    11 -> R.drawable.characterfone11; 12 -> R.drawable.characterfone12
    else -> error("Неизвестный номер characterfone: $n")
}

// ─── Получение названия фото по порядковому номеру ───────────────────────────
@Composable
private fun photoLabel(id: Int): String {
    val n = when {
        id in 201..215 -> id - 200
        id in 301..316 -> id - 300
        id in 401..412 -> id - 400
        else -> 0
    }
    return "$n"
}

// ─── Маппинг ключа градиента → R.string.* ────────────────────────────────────
// Обычная функция (не Composable) — try-catch в Composable запрещён
fun gradientNameResId(nameKey: String): Int = when (nameKey) {
    "bg_none" -> R.string.bg_none
    "bg_1"    -> R.string.bg_1
    "bg_2"    -> R.string.bg_2
    "bg_3"    -> R.string.bg_3
    "bg_4"    -> R.string.bg_4
    "bg_5"    -> R.string.bg_5
    "bg_6"    -> R.string.bg_6
    "bg_7"    -> R.string.bg_7
    "bg_8"    -> R.string.bg_8
    "bg_9"    -> R.string.bg_9
    "bg_10"   -> R.string.bg_10
    "bg_11"   -> R.string.bg_11
    "bg_12"   -> R.string.bg_12
    "bg_13"   -> R.string.bg_13
    "bg_14"   -> R.string.bg_14
    "bg_15"   -> R.string.bg_15
    else      -> R.string.bg_none
}

// ─── Пикер фона ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerSheet(
    currentBackgroundId: Int,
    backgroundType: DreamBackgrounds.Type = DreamBackgrounds.Type.DREAM,
    onBackgroundSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val photoList = DreamBackgrounds.photosForType(backgroundType)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF120820),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoldLight.copy(0.3f))
            )
        }
    ) {
        // ── Заголовок ──
        Text(
            text = stringResource(R.string.bg_picker_title),
            color = GoldLight,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // ── Секция: Фотографии ──
        Text(
            text = stringResource(R.string.bg_section_photos),
            color = GoldLight.copy(0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(photoList) { photo ->
                val isSelected = photo.id == currentBackgroundId
                val resId = photoResId(photo.id)
                Box(
                    modifier = Modifier
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.5.dp,
                            color = if (isSelected) GoldLight else Color.White.copy(0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onBackgroundSelected(photo.id) }
                ) {
                    if (resId != null) {
                        Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(Color(0xFF1A0C30)))
                    }
                    // Тонкое затемнение снизу
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.5f)),
                                    startY = 0.5f
                                )
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = photoLabel(photo.id),
                            color = Color.White.copy(0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    // Галочка
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(GoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 10.sp, color = Color(0xFF1A0C30), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Разделитель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldLight.copy(0.25f), Color.Transparent)
                    )
                )
        )

        // ── Секция: Градиенты ──
        Text(
            text = stringResource(R.string.bg_section_gradients),
            color = GoldLight.copy(0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            items(DreamBackgrounds.gradients) { bg ->
                val isSelected = bg.id == currentBackgroundId
                val nameResId = gradientNameResId(bg.nameKey)

                Box(
                    modifier = Modifier
                        .aspectRatio(0.72f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (bg.id == 0)
                                Brush.verticalGradient(listOf(Color(0xFF1A1030), Color(0xFF0A0820)))
                            else bg.brush
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.5.dp,
                            color = if (isSelected) GoldLight else Color.White.copy(0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onBackgroundSelected(bg.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (bg.id == 0) {
                        // "Без фона" — иконка запрета
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = Color.White.copy(0.35f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(nameResId),
                                color = Color.White.copy(0.5f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    } else {
                        // Красивое название поверх градиента
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(0.6f)),
                                        startY = 0.4f
                                    )
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = stringResource(nameResId),
                                color = Color.White.copy(0.90f),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                lineHeight = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                            )
                        }
                    }
                    // Галочка
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(5.dp)
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(GoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 10.sp, color = Color(0xFF1A0C30), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
