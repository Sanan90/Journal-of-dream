package com.dreamjournal.journalofdream.ui.theme

import com.dreamjournal.journalofdream.util.localizeCategory
import com.dreamjournal.journalofdream.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.ui.common.BackgroundPickerSheet
import com.dreamjournal.journalofdream.ui.common.DreamBackgrounds
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

private val GoldLight      = Color(0xFFF0D68C)
private val GoldDark       = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val CardGradient   = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.78f), Color(0xFF1A0C30).copy(alpha = 0.90f))
)

@Composable
fun ViewDreamScreen(
    navController: NavHostController,
    dreamId: String,
    dreamViewModel: DreamViewModel
) {
    val dreamIdInt = dreamId.toIntOrNull() ?: return
    val dreamWithLocs by dreamViewModel.getDreamWithLocationsById(dreamIdInt).observeAsState()

    var visible by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }

    LaunchedEffect(dreamWithLocs) { if (dreamWithLocs != null) visible = true }

    val dream = dreamWithLocs?.dream

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Фон приложения ──
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ── Фон сна поверх (если выбран) ──
        DreamBackgroundLayer(dream?.backgroundId ?: 0)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Заголовок ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back),
                        tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = stringResource(R.string.dream_title),
                    color = GoldLight,
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.weight(1f)
                )
                // Кнопка выбора фона
                IconButton(onClick = { showBackgroundPicker = true }) {
                    Icon(Icons.Default.Palette, "Фон",
                        tint = if ((dream?.backgroundId ?: 0) != 0) GoldLight else GoldLight.copy(0.4f),
                        modifier = Modifier.size(22.dp))
                }
                // Кнопка редактирования
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(
                            listOf(Color(0xFF7B3FA0).copy(0.70f), Color(0xFF4A2870).copy(0.70f))
                        ))
                        .border(1.dp, GoldLight.copy(0.40f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { navController.navigate("editDream/$dreamId") }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.btn_edit),
                            tint = GoldLight, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
            }

            // ── Контент ──
            when {
                dreamWithLocs == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldLight, strokeWidth = 2.dp)
                    }
                }
                else -> {
                    val dwl = dreamWithLocs!!

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(400)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))

                            // ── Название сна ──
                            Text(
                                text = dwl.dream.title.ifBlank { stringResource(R.string.dream_no_title) },
                                color = GoldLight,
                                fontFamily = PlayfairFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                lineHeight = 34.sp
                            )

                            // ── Мета-карточка ──
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(CardGradient)
                                    .border(1.dp, GoldLight.copy(0.25f), RoundedCornerShape(20.dp))
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(2.dp).background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, GoldLight.copy(0.55f), GoldDark.copy(0.55f), Color.Transparent)
                                        )
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    DreamMetaItem(R.drawable.dream_view_date, stringResource(R.string.dream_label_date), formatDreamDate(dwl.dream.date))
                                    if (dwl.dream.time.isNotBlank()) {
                                        Box(Modifier.width(1.dp).height(50.dp).background(Color.White.copy(0.12f)))
                                        DreamMetaItem(R.drawable.dream_view_time, stringResource(R.string.dream_label_time), dwl.dream.time)
                                    }
                                    if (dwl.dream.category.isNotBlank()) {
                                        Box(Modifier.width(1.dp).height(50.dp).background(Color.White.copy(0.12f)))
                                        DreamMetaItem(
                                            R.drawable.dream_view_category,
                                            stringResource(R.string.dream_label_category),
                                            localizeCategory(dwl.dream.category,
                                                stringResource(R.string.dreams_no_category),
                                                stringResource(R.string.cat_nightmares),
                                                stringResource(R.string.cat_lucid),
                                                stringResource(R.string.cat_plot),
                                                stringResource(R.string.cat_personal))
                                        )
                                    }
                                }
                            }

                            // ── Локации ──
                            if (dwl.locations.isNotEmpty()) {
                                DreamTagCard(R.drawable.location_icon_gold, stringResource(R.string.dream_label_locations), dwl.locations.map { it.name })
                            }

                            // ── Персонажи ──
                            if (dwl.characters.isNotEmpty()) {
                                DreamTagCard(R.drawable.dreamers_icon, stringResource(R.string.dream_label_characters), dwl.characters.map { it.name })
                            }

                            // ── Текст сна ──
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.verticalGradient(
                                        listOf(Color(0xFF2E1448).copy(alpha = 0.82f), Color(0xFF130830).copy(alpha = 0.92f))
                                    ))
                                    .border(1.dp, Brush.verticalGradient(
                                        listOf(GoldLight.copy(0.35f), GoldDark.copy(0.12f))
                                    ), RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 14.dp)) {
                                        Image(painterResource(R.drawable.dream_book_icon), null,
                                            Modifier.size(22.dp), contentScale = ContentScale.Fit)
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.dream_content_label),
                                            color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(
                                        Brush.horizontalGradient(listOf(GoldLight.copy(0.40f), Color.Transparent))
                                    ))
                                    Spacer(Modifier.height(14.dp))
                                    if (dwl.dream.content.isBlank()) {
                                        Text(stringResource(R.string.dream_no_desc),
                                            color = Color.White.copy(0.38f), fontSize = 16.sp,
                                            fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth())
                                    } else {
                                        Text(dwl.dream.content, color = Color.White.copy(0.92f),
                                            fontSize = 16.sp, lineHeight = 26.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // ── Пикер фона ──
    if (showBackgroundPicker && dream != null) {
        BackgroundPickerSheet(
            currentBackgroundId = dream.backgroundId,
            backgroundType = DreamBackgrounds.Type.DREAM,
            onBackgroundSelected = { newId ->
                dreamViewModel.updateDream(
                    dream.copy(backgroundId = newId),
                    emptyList(), emptyList()
                )
                showBackgroundPicker = false
            },
            onDismiss = { showBackgroundPicker = false }
        )
    }
}

@Composable
private fun DreamMetaItem(iconRes: Int, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(iconRes), null, Modifier.size(28.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(5.dp))
        Text(label, color = Color.White.copy(0.50f), fontSize = 11.sp, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DreamTagCard(iconRes: Int, title: String, items: List<String>) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(CardGradient).border(1.dp, GoldLight.copy(0.20f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(iconRes), null, Modifier.size(20.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(8.dp))
                Text(title, color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            items.forEach { name ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(Modifier.size(5.dp).background(GoldDark.copy(0.80f), RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(name, color = Color.White.copy(0.90f), fontSize = 15.sp)
                }
            }
        }
    }
}

private fun formatDreamDate(date: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(date)!!)
    } catch (e: Exception) { date }
}

// ─── Универсальный слой фона (градиент или фото) ──────────────────────────────
@Composable
fun DreamBackgroundLayer(backgroundId: Int) {
    if (backgroundId == 0) return
    if (DreamBackgrounds.isPhoto(backgroundId)) {
        val resId = com.dreamjournal.journalofdream.ui.common.photoResId(backgroundId) ?: return
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.30f)),
            contentScale = ContentScale.Crop,
            alpha = 0.70f
        )
    } else {
        val brush = DreamBackgrounds.getBrush(backgroundId) ?: return
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.35f))
        )
    }
}
