package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dreamjournal.journalofdream.util.LocaleHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Technique
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.TechniqueViewModel

// Секретный код для входа в режим админа
private const val ADMIN_TAP_COUNT = 7

enum class TechniqueSort { BY_LIKES, BY_NEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniquesScreen(
    navController: NavHostController,
    isAdminMode: Boolean = false,
    onAdminModeChanged: (Boolean) -> Unit = {}
) {
    val viewModel: TechniqueViewModel = viewModel()
    val techniques by viewModel.techniques.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(true)
    val userVotes by viewModel.userVotes.observeAsState(emptyMap())
    val context = LocalContext.current

    // Сортировка
    var sort by rememberSaveable { mutableStateOf(TechniqueSort.BY_LIKES) }

    // Режим админа
    var tapCount by remember { mutableIntStateOf(0) }
    var adminCodeFromServer by remember { mutableStateOf<String?>(null) }

    // Диалоги
    var showAdminCodeDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTechnique by remember { mutableStateOf<Technique?>(null) }
    var deletingTechnique by remember { mutableStateOf<Technique?>(null) }
    var selectedTechnique by remember { mutableStateOf<Technique?>(null) }

    // Отсортированный список
    val sortedTechniques = remember(techniques, sort) {
        when (sort) {
            TechniqueSort.BY_LIKES -> techniques.sortedByDescending { it.likes - it.dislikes }
            TechniqueSort.BY_NEW -> techniques.sortedByDescending { it.createdAt }
        }
    }

    // Диалог ввода кода
    if (showAdminCodeDialog) {
        AdminCodeDialog(
            onConfirm = { enteredCode, setError ->
                val cached = adminCodeFromServer
                if (cached == null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("config")
                        .document("admin")
                        .get()
                        .addOnSuccessListener { doc ->
                            val serverCode = doc.getString("code") ?: ""
                            adminCodeFromServer = serverCode
                            if (enteredCode == serverCode) {
                                showAdminCodeDialog = false
                                onAdminModeChanged(true)
                                tapCount = 0
                            } else {
                                setError(context.getString(R.string.tech_wrong_code))
                            }
                        }
                        .addOnFailureListener {
                            setError(context.getString(R.string.tech_conn_error))
                        }
                } else {
                    if (enteredCode == cached) {
                        showAdminCodeDialog = false
                        onAdminModeChanged(true)
                        tapCount = 0
                    } else {
                        setError(context.getString(R.string.tech_wrong_code))
                    }
                }
            },
            onDismiss = {
                showAdminCodeDialog = false
                tapCount = 0
            }
        )
    }

    // BottomSheet с полным описанием и комментариями
    selectedTechnique?.let { technique ->
        // Актуальные данные техники из списка (лайки обновляются в реальном времени)
        val actualTechnique = techniques.find { it.id == technique.id } ?: technique
        TechniqueDetailSheet(
            technique = actualTechnique,
            userVote = userVotes[actualTechnique.id],
            isAdminMode = isAdminMode,
            onLike = { viewModel.vote(actualTechnique.id, true) },
            onDislike = { viewModel.vote(actualTechnique.id, false) },
            onDismiss = { selectedTechnique = null }
        )
    }

    // Диалог добавления/редактирования
    if (showAddDialog || editingTechnique != null) {
        TechniqueEditDialog(
            technique = editingTechnique,
            onConfirm = { name, description, source ->
                if (editingTechnique != null) {
                    viewModel.updateTechnique(editingTechnique!!, name, description, source)
                } else {
                    viewModel.addTechnique(name, description, source)
                }
                showAddDialog = false
                editingTechnique = null
            },
            onDismiss = {
                showAddDialog = false
                editingTechnique = null
            }
        )
    }

    // Диалог подтверждения удаления
    if (deletingTechnique != null) {
        AlertDialog(
            onDismissRequest = { deletingTechnique = null },
            title = { Text(stringResource(R.string.technique_delete_title)) },
            text = { Text(stringResource(R.string.technique_delete_message, deletingTechnique!!.localizedName(LocalContext.current.resources.configuration.locales[0].language))) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTechnique(deletingTechnique!!.id)
                    deletingTechnique = null
                }) { Text(stringResource(R.string.btn_delete), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deletingTechnique = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Нажатие 7 раз — полностью невидимое, без ripple эффекта
                    Text(
                        text = if (isAdminMode) "🔧 " + stringResource(R.string.nav_techniques) + " (Admin)" else stringResource(R.string.nav_techniques),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isAdminMode) {
                                tapCount++
                                if (tapCount >= ADMIN_TAP_COUNT) {
                                    showAdminCodeDialog = true
                                    tapCount = 0
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                actions = {
                    // Кнопка добавить — только в режиме админа
                    AnimatedVisibility(
                        visible = isAdminMode,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, stringResource(R.string.btn_add), tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (sortedTechniques.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.tech_empty),
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Переключатель сортировки
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = sort == TechniqueSort.BY_LIKES,
                                onClick = { sort = TechniqueSort.BY_LIKES },
                                label = { Text("👍 " + stringResource(R.string.locations_sort_popular), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7E57C2),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Black.copy(alpha = 0.3f),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            FilterChip(
                                selected = sort == TechniqueSort.BY_NEW,
                                onClick = { sort = TechniqueSort.BY_NEW },
                                label = { Text("🆕 " + stringResource(R.string.tech_sort_new), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7E57C2),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Black.copy(alpha = 0.3f),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(sortedTechniques, key = { it.id }) { technique ->
                        val userVote = userVotes[technique.id]
                        TechniqueCard(
                            technique = technique,
                            userVote = userVote,
                            isAdminMode = isAdminMode,
                            onLike = { viewModel.vote(technique.id, true) },
                            onDislike = { viewModel.vote(technique.id, false) },
                            onEdit = { editingTechnique = technique },
                            onDelete = { deletingTechnique = technique },
                            onClick = { selectedTechnique = technique }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun TechniqueCard(
    technique: Technique,
    userVote: String?,
    isAdminMode: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocaleHelper.getSavedLanguage(context).let { if (it == "system") context.resources.configuration.locales[0].language else it }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Заголовок + кнопки админа
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = technique.localizedName(lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isAdminMode) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, stringResource(R.string.btn_edit),
                            tint = Color(0xFF7E57C2), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, stringResource(R.string.btn_delete),
                            tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Описание — максимум 4 строки, остальное в BottomSheet
            Text(
                text = technique.localizedDescription(lang),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp,
                maxLines = 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Источник если есть
            if (technique.source.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📚 ${technique.source}",
                    fontSize = 12.sp,
                    color = Color(0xFF7E57C2),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Лайки, дизлайки и счётчик комментариев
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Лайк
                VoteButton(
                    emoji = "👍",
                    count = technique.likes,
                    isActive = userVote == "like",
                    activeColor = Color(0xFF4CAF50),
                    onClick = onLike
                )
                // Дизлайк
                VoteButton(
                    emoji = "👎",
                    count = technique.dislikes,
                    isActive = userVote == "dislike",
                    activeColor = Color(0xFFEF5350),
                    onClick = onDislike
                )

                Spacer(modifier = Modifier.weight(1f))

                // Счётчик комментариев — всегда виден
                Text(
                    text = "💬 ${technique.commentsCount}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )

                // Рейтинг
                val rating = technique.likes - technique.dislikes
                Text(
                    text = if (rating > 0) "+$rating" else "$rating",
                    color = when {
                        rating > 0 -> Color(0xFF4CAF50)
                        rating < 0 -> Color(0xFFEF5350)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun VoteButton(
    emoji: String,
    count: Int,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                if (isActive) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) activeColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count",
            color = if (isActive) activeColor else Color.Gray,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun AdminCodeDialog(
    onConfirm: (code: String, setError: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔐 " + stringResource(R.string.tech_enter_code), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            code = it
                            error = null
                        }
                    },
                    label = { Text(stringResource(R.string.tech_access_code)) },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error!!, color = Color.Red) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Button(onClick = {
                        onConfirm(code) { msg ->
                            error = msg
                            code = ""
                        }
                    }) { Text(stringResource(R.string.btn_login)) }
                }
            }
        }
    }
}

@Composable
fun TechniqueEditDialog(
    technique: Technique?,
    onConfirm: (name: String, description: String, source: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(technique?.name ?: "") }
    var description by remember { mutableStateOf(technique?.description ?: "") }
    var source by remember { mutableStateOf(technique?.source ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (technique != null) stringResource(R.string.tech_edit_title) else stringResource(R.string.tech_add_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text(stringResource(R.string.dream_field_name) + " *") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descError = false },
                    label = { Text(stringResource(R.string.dream_field_desc) + " *") },
                    isError = descError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text(stringResource(R.string.tech_source_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.tech_source_hint)) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        nameError = name.isBlank()
                        descError = description.isBlank()
                        if (!nameError && !descError) {
                            onConfirm(name, description, source)
                        }
                    }) { Text(stringResource(R.string.btn_save)) }
                }
            }
        }
    }
}
