package com.example.journalofdream.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen

data class Technique(
    val id: String,
    val name: String,
    val description: String
)

private val techniques = listOf(
    Technique("wild", "WILD", "Wake-Initiated Lucid Dream — вход в осознанный сон напрямую из бодрствования. Лечь, расслабиться и удерживать сознание пока тело засыпает."),
    Technique("mild", "MILD", "Mnemonic Induction — перед сном повторять установку \"Я буду осознавать что сплю\". Визуализировать прошлый сон и момент осознания."),
    Technique("dild", "DILD", "Dream-Initiated Lucid Dream — осознание внутри сна через проверку реальности: смотреть на руки, читать текст дважды, нажимать на ладонь пальцем."),
    Technique("wbtb", "WBTB", "Wake Back To Bed — проснуться через 5-6 часов сна, пободрствовать 20-30 минут, затем снова лечь. Резко повышает шанс осознанного сна."),
    Technique("fild", "FILD", "Finger Induced — сразу после пробуждения слегка шевелить двумя пальцами поочерёдно (имитируя игру на пианино) и не засыпать полностью."),
    Technique("deild", "DEILD", "Dream Exit Induced — при пробуждении из сна не двигаться и не открывать глаза, сразу войти обратно в тот же сон осознанно."),
    Technique("reality", "Проверки реальности", "Регулярно в течение дня спрашивать себя \"Сплю ли я?\". Смотреть на часы дважды, читать текст, зажимать нос и пробовать дышать."),
    Technique("journal", "Дневник снов", "Записывать сны сразу после пробуждения — улучшает память снов и помогает замечать повторяющиеся элементы (триггеры осознания)."),
    Technique("stabilization", "Стабилизация", "Когда осознал что спишь — потереть руки, потрогать предметы вокруг, крикнуть \"Стабильность!\". Не смотреть в одну точку — сон растворится."),
    Technique("ssp", "Сенсорная депривация", "Использовать маску для сна и беруши — убрать внешние раздражители. Помогает войти в более глубокий сон и увеличивает яркость снов."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniquesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("techniques_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Загружаем состояние изученных техник из SharedPreferences
    val learnedTechniques = remember {
        mutableStateMapOf<String, Boolean>().apply {
            techniques.forEach { t ->
                put(t.id, prefs.getBoolean(t.id, false))
            }
        }
    }

    val learnedCount = learnedTechniques.values.count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Техники", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Прогресс изучения
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Изучено: $learnedCount из ${techniques.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LinearProgressIndicator(
                        progress = { learnedCount.toFloat() / techniques.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(techniques) { technique ->
                    val isLearned = learnedTechniques[technique.id] == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLearned)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = technique.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLearned)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = technique.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = {
                                    val newValue = !isLearned
                                    learnedTechniques[technique.id] = newValue
                                    prefs.edit().putBoolean(technique.id, newValue).apply()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isLearned)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = if (isLearned) "Изучено" else "Не изучено",
                                    tint = if (isLearned)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
