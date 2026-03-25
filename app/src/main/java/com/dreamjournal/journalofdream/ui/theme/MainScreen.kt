package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.dreamjournal.journalofdream.R

private val GoldLight = Color(0xFFF0D68C)

private val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    isGuest: Boolean,
    displayName: String?,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    characterViewModel: CharacterViewModel = viewModel()
) {
    val allDreams by dreamViewModel.dreams.observeAsState(emptyList())
    val allLocations by locationViewModel.locations.observeAsState(emptyList())
    val allCharacters by characterViewModel.characters.observeAsState(emptyList())

    var isVisible by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val lucidPercent = remember(allDreams) {
        if (allDreams.isEmpty()) 0
        else {
            val c = allDreams.count {
                it.category.contains("Осознанные", true) || it.category.contains("Lucid", true)
            }
            (c * 100) / allDreams.size
        }
    }

    val dreamsThisMonth = remember(allDreams) {
        val now = java.util.Calendar.getInstance()
        val y = now.get(java.util.Calendar.YEAR)
        val m = now.get(java.util.Calendar.MONTH) + 1
        allDreams.count {
            try { val p = it.date.split("-"); p[0].toInt() == y && p[1].toInt() == m }
            catch (e: Exception) { false }
        }
    }

    val fabTransition = rememberInfiniteTransition(label = "fab_glow")
    val fabGlowAlpha by fabTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fab_alpha"
    )

    // Анимация нажатия кнопки "+"
    var fabPressed by remember { mutableStateOf(false) }
    val fabScale by animateFloatAsState(
        targetValue = if (fabPressed) 1.15f else 1f,
        animationSpec = tween(150),
        label = "fab_press"
    )

    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══ ВЕРХНЯЯ ПАНЕЛЬ ═══
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGuest) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.main_guest), color = Color.White.copy(0.7f), fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        TextButton(onClick = { navController.navigate("auth") }) {
                            Text(stringResource(R.string.btn_login), fontSize = 12.sp)
                        }
                    }
                } else Spacer(Modifier.width(1.dp))

                if (!isGuest) {

                }
            }

            // ═══ ЗАГОЛОВОК ═══
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.8f, animationSpec = tween(600))
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.nav_diary),
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = GoldLight
                    )
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(R.drawable.elegant_golden_moon),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (!isGuest) {
                Text(
                    stringResource(R.string.main_dreamer, displayName ?: ""),
                    color = Color.White.copy(0.85f), fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ═══════════════════════════════════════
            // ═══ ХРУСТАЛЬНЫЙ ШАР ═════════════════
            // ═══════════════════════════════════════
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.85f, animationSpec = tween(800))
            ) {
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1A1445).copy(alpha = 0.50f),
                                        Color(0xFF0D0B2E).copy(alpha = 0.60f),
                                        Color(0xFF1A1040).copy(alpha = 0.70f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("${allDreams.size}", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(stringResource(R.string.plural_dreams_many), fontSize = 15.sp, color = Color.White.copy(0.85f))
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(0.88f), horizontalArrangement = Arrangement.SpaceEvenly) {
                                OrbStatIcon(R.drawable.location_icon_gold, "${allLocations.size}", stringResource(R.string.nav_locations))
                                OrbDivider()
                                OrbStatIcon(R.drawable.magic_glass_icon, "${allCharacters.size}", stringResource(R.string.nav_characters))
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(0.88f), horizontalArrangement = Arrangement.SpaceEvenly) {
                                OrbStatEmoji("🌙", "$lucidPercent%", stringResource(R.string.stats_lucid_short))
                                OrbDivider()
                                OrbStatEmoji("📅", "$dreamsThisMonth", stringResource(R.string.stats_this_month))
                            }
                        }
                    }

                    Image(
                        painter = painterResource(R.drawable.crystal_glass),
                        contentDescription = null,
                        modifier = Modifier.size(220.dp).clip(RoundedCornerShape(24.dp)).alpha(0.06f),
                        contentScale = ContentScale.Crop
                    )

                    Image(
                        painter = painterResource(R.drawable.squad_ram),
                        contentDescription = null,
                        modifier = Modifier.size(300.dp).alpha(0.55f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ═══════════════════════════════════════
            // ═══ КНОПКИ НАВИГАЦИИ 2×2 ════════════
            // ═══════════════════════════════════════
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavButton(Modifier.weight(1f), stringResource(R.string.nav_diary), R.drawable.dream_journal_icon, R.drawable.dreamy_purple_blue) {
                    navController.navigate("dreams")
                }
                NavButton(Modifier.weight(1f), stringResource(R.string.nav_locations), R.drawable.location_icon, R.drawable.mystical_pink) {
                    navController.navigate("locations")
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavButton(Modifier.weight(1f), stringResource(R.string.nav_characters), R.drawable.dreamers_icon, R.drawable.soft_lavender) {
                    navController.navigate("characters")
                }
                NavButton(Modifier.weight(1f), stringResource(R.string.nav_techniques), R.drawable.magic_book_icon, R.drawable.deep_indigo) {
                    navController.navigate("techniques")
                }
            }

            Spacer(Modifier.weight(1f))

            // ═══════════════════════════════════════
            // ═══ НИЖНЯЯ ПАНЕЛЬ ═══════════════════
            // ═══════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2E2060).copy(alpha = 0.85f),
                                    Color(0xFF1C1045).copy(alpha = 0.92f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(top = 14.dp, bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BarItemIcon(R.drawable.stats_icon, stringResource(R.string.nav_stats)) { navController.navigate("stats") }
                        BarItemIcon(R.drawable.graphic_cosmo_icon, stringResource(R.string.nav_chart)) { navController.navigate("chart") }
                        Spacer(Modifier.width(80.dp))
                        BarItemEmoji("🏆", stringResource(R.string.nav_achievements)) { navController.navigate("achievements") }
                        BarItemIcon(R.drawable.setting_icon2, stringResource(R.string.settings_title)) { navController.navigate("settings") }
                    }
                }

                // Кнопка "+" — увеличена вдвое (120dp), с анимацией нажатия
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-10).dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Свечение
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(fabGlowAlpha)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF9C27B0).copy(alpha = 0.5f), Color.Transparent)),
                                CircleShape
                            )
                    )
                    // Сама кнопка с картинкой
                    Image(
                        painter = painterResource(R.drawable.add_button),
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .scale(fabScale)
                            .clip(CircleShape)
                            .shadow(10.dp, CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        fabPressed = true
                                        tryAwaitRelease()
                                        fabPressed = false
                                        showDialog = true
                                    }
                                )
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // ═══ ДИАЛОГИ ═══
        if (showDialog) {
            ChooseActionDialog(
                onDismiss = { showDialog = false },
                onDreamSelected = { navController.navigate("addDream"); showDialog = false },
                onLocationSelected = { navController.navigate("addLocation"); showDialog = false },
                onCharacterSelected = { navController.navigate("addCharacter"); showDialog = false }
            )
        }

    }
}

// ═══ КОМПОНЕНТЫ ═══

@Composable
private fun OrbStatIcon(iconRes: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(22.dp), contentScale = ContentScale.Fit)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun OrbStatEmoji(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun OrbDivider() {
    Box(Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.15f)))
}

@Composable
private fun NavButton(
    modifier: Modifier = Modifier,
    text: String,
    iconRes: Int,
    backgroundRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(58.dp).shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            )
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent, Color.Black.copy(alpha = 0.15f))),
                RoundedCornerShape(22.dp)
            ))
            Box(Modifier.fillMaxSize().border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.2f))),
                RoundedCornerShape(22.dp)
            ))
            Row(
                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(8.dp))
                AutoSizeText(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxFontSize = 13.sp,
                    minFontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun BarItemIcon(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(28.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun BarItemEmoji(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun AutoSizeText(
    text: String, color: Color, fontWeight: FontWeight,
    maxFontSize: androidx.compose.ui.unit.TextUnit, minFontSize: androidx.compose.ui.unit.TextUnit
) {
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    Text(
        text = text, color = color, fontSize = fontSize, fontWeight = fontWeight,
        maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false,
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontSize > minFontSize) fontSize = (fontSize.value - 1f).sp
            else readyToDraw = true
        },
        modifier = Modifier.alpha(if (readyToDraw) 1f else 0f)
    )
}
