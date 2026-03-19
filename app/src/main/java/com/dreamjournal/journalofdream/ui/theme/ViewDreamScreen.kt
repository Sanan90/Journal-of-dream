package com.dreamjournal.journalofdream.ui.theme

import com.dreamjournal.journalofdream.util.localizeCategory
import com.dreamjournal.journalofdream.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDreamScreen(
    navController: NavHostController,
    dreamId: String,
    dreamViewModel: DreamViewModel
) {
    val dreamIdInt = dreamId.toIntOrNull() ?: return
    val dreamWithLocs by dreamViewModel.getDreamWithLocationsById(dreamIdInt).observeAsState()

    // Анимация появления контента
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(dreamWithLocs) {
        if (dreamWithLocs != null) visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dream_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("editDream/$dreamId")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            dreamWithLocs?.let { dwl ->
                val dream = dwl.dream

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
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Заголовок
                        Text(
                            text = dream.title.ifBlank { stringResource(R.string.dream_no_title) },
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp
                        )

                        // Мета-информация: дата, время, категория
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Дата
                                MetaItem(
                                    emoji = "📅",
                                    label = stringResource(R.string.dream_label_date),
                                    value = formatDreamDate(dream.date)
                                )
                                // Время
                                if (dream.time.isNotBlank()) {
                                    MetaItem(
                                        emoji = "🕐",
                                        label = stringResource(R.string.dream_label_time),
                                        value = dream.time
                                    )
                                }
                                // Категория
                                if (dream.category.isNotBlank()) {
                                    MetaItem(
                                        emoji = "🏷️",
                                        label = stringResource(R.string.dream_label_category),
                                        value = localizeCategory(dream.category, stringResource(R.string.dreams_no_category), stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid), stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))
                                    )
                                }
                            }
                        }

                        // Локации если есть
                        if (dwl.locations.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "📍 " + stringResource(R.string.dream_label_locations),
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    dwl.locations.forEach { location ->
                                        Text(
                                            text = "• ${location.name}",
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }


                        if (dwl.characters.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "👤 " + stringResource(R.string.dream_label_characters),
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    dwl.characters.forEach { character ->
                                        Text(
                                            text = "• ${character.name}",
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Текст сна
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.2f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                if (dream.content.isBlank()) {
                                    Text(
                                        text = stringResource(R.string.dream_no_desc),
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 16.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = dream.content,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 16.sp,
                                        lineHeight = 26.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } ?: run {
                // Загрузка
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MetaItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDreamDate(date: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(date)!!)
    } catch (e: Exception) { date }
}
