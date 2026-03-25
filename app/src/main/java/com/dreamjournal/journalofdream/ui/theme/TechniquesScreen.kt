package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.dreamjournal.journalofdream.util.LocaleHelper
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Technique
import com.dreamjournal.journalofdream.viewmodel.TechniqueViewModel

private const val ADMIN_TAP_COUNT = 7

private val GoldLight      = Color(0xFFF0D68C)
private val GoldDark       = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val TechCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.78f), Color(0xFF1A0C30).copy(alpha = 0.90f))
)

enum class TechniqueSort { BY_LIKES, BY_NEW }

@Composable
fun TechniquesScreen(
    navController: NavHostController,
    isAdminMode: Boolean = false,
    onAdminModeChanged: (Boolean) -> Unit = {}
) {
    val viewModel: TechniqueViewModel = viewModel()
    val techniques  by viewModel.techniques.observeAsState(emptyList())
    val isLoading   by viewModel.isLoading.observeAsState(true)
    val userVotes   by viewModel.userVotes.observeAsState(emptyMap())
    val context     = LocalContext.current

    var sort by rememberSaveable { mutableStateOf(TechniqueSort.BY_LIKES) }
    var tapCount by remember { mutableIntStateOf(0) }
    var adminCodeFromServer by remember { mutableStateOf<String?>(null) }
    var showAdminCodeDialog by remember { mutableStateOf(false) }
    var showAddDialog       by remember { mutableStateOf(false) }
    var editingTechnique    by remember { mutableStateOf<Technique?>(null) }
    var deletingTechnique   by remember { mutableStateOf<Technique?>(null) }
    var selectedTechnique   by remember { mutableStateOf<Technique?>(null) }

    val sortedTechniques = remember(techniques, sort) {
        when (sort) {
            TechniqueSort.BY_LIKES -> techniques.sortedByDescending { it.likes - it.dislikes }
            TechniqueSort.BY_NEW   -> techniques.sortedByDescending { it.createdAt }
        }
    }

    if (showAdminCodeDialog) {
        AdminCodeDialog(
            onConfirm = { enteredCode, setError ->
                val cached = adminCodeFromServer
                if (cached == null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("config").document("admin").get()
                        .addOnSuccessListener { doc ->
                            val serverCode = doc.getString("code") ?: ""
                            adminCodeFromServer = serverCode
                            if (enteredCode == serverCode) { showAdminCodeDialog = false; onAdminModeChanged(true); tapCount = 0 }
                            else setError(context.getString(R.string.tech_wrong_code))
                        }
                        .addOnFailureListener { setError(context.getString(R.string.tech_conn_error)) }
                } else {
                    if (enteredCode == cached) { showAdminCodeDialog = false; onAdminModeChanged(true); tapCount = 0 }
                    else setError(context.getString(R.string.tech_wrong_code))
                }
            },
            onDismiss = { showAdminCodeDialog = false; tapCount = 0 }
        )
    }

    selectedTechnique?.let { technique ->
        val actual = techniques.find { it.id == technique.id } ?: technique
        TechniqueDetailSheet(
            technique = actual, userVote = userVotes[actual.id], isAdminMode = isAdminMode,
            onLike = { viewModel.vote(actual.id, true) },
            onDislike = { viewModel.vote(actual.id, false) },
            onDismiss = { selectedTechnique = null }
        )
    }

    if (showAddDialog || editingTechnique != null) {
        TechniqueEditDialog(
            technique = editingTechnique,
            onConfirm = { name, description, source ->
                if (editingTechnique != null) viewModel.updateTechnique(editingTechnique!!, name, description, source)
                else viewModel.addTechnique(name, description, source)
                showAddDialog = false; editingTechnique = null
            },
            onDismiss = { showAddDialog = false; editingTechnique = null }
        )
    }

    if (deletingTechnique != null) {
        AlertDialog(
            onDismissRequest = { deletingTechnique = null },
            title = { Text(stringResource(R.string.technique_delete_title)) },
            text  = { Text(stringResource(R.string.technique_delete_message,
                deletingTechnique!!.localizedName(LocalContext.current.resources.configuration.locales[0].language))) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTechnique(deletingTechnique!!.id); deletingTechnique = null }) {
                    Text(stringResource(R.string.btn_delete), color = Color.Red)
                }
            },
            dismissButton = { TextButton(onClick = { deletingTechnique = null }) { Text(stringResource(R.string.btn_cancel)) } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // ── Заголовок ──
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back), tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = if (isAdminMode) "🔧 " + stringResource(R.string.nav_techniques) + " (Admin)"
                           else stringResource(R.string.nav_techniques),
                    color = GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp,
                    modifier = Modifier.weight(1f).clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {
                        if (!isAdminMode) { tapCount++; if (tapCount >= ADMIN_TAP_COUNT) { showAdminCodeDialog = true; tapCount = 0 } }
                    }
                )
                Image(painterResource(R.drawable.magic_book_icon), null,
                    Modifier.size(34.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
                AnimatedVisibility(visible = isAdminMode, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, stringResource(R.string.btn_add), tint = GoldLight)
                    }
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldLight, strokeWidth = 2.dp)
                }
                sortedTechniques.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(R.drawable.magic_book_icon), null, Modifier.size(72.dp))
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.tech_empty), color = Color.White.copy(0.55f),
                            textAlign = TextAlign.Center, fontSize = 15.sp)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TechSortChip("👍 " + stringResource(R.string.locations_sort_popular), sort == TechniqueSort.BY_LIKES) { sort = TechniqueSort.BY_LIKES }
                            TechSortChip("🆕 " + stringResource(R.string.tech_sort_new), sort == TechniqueSort.BY_NEW) { sort = TechniqueSort.BY_NEW }
                        }
                    }
                    items(sortedTechniques, key = { it.id }) { technique ->
                        TechniqueCard(
                            technique = technique, userVote = userVotes[technique.id], isAdminMode = isAdminMode,
                            onLike = { viewModel.vote(technique.id, true) }, onDislike = { viewModel.vote(technique.id, false) },
                            onEdit = { editingTechnique = technique }, onDelete = { deletingTechnique = technique },
                            onClick = { selectedTechnique = technique }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TechSortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (selected) Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870)))
                        else Brush.horizontalGradient(listOf(Color(0xFF2A1545).copy(0.70f), Color(0xFF1A0D30).copy(0.70f))))
            .border(1.dp, if (selected) GoldLight.copy(0.65f) else Color.White.copy(0.15f), RoundedCornerShape(12.dp))
            .clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) GoldLight else Color.White.copy(0.65f))
    }
}

@Composable
fun TechniqueCard(
    technique: Technique, userVote: String?, isAdminMode: Boolean,
    onLike: () -> Unit, onDislike: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocaleHelper.getSavedLanguage(context).let { if (it == "system") context.resources.configuration.locales[0].language else it }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(TechCardGradient)
            .border(1.dp, Brush.verticalGradient(listOf(GoldLight.copy(0.30f), GoldDark.copy(0.10f))), RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        // Золотая линия сверху
        Box(Modifier.fillMaxWidth().height(2.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent, GoldLight.copy(0.50f), GoldDark.copy(0.50f), Color.Transparent))))

        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.magic_book_icon), null, Modifier.size(26.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(10.dp))
                Text(technique.localizedName(lang), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = GoldLight, fontFamily = PlayfairFamily, modifier = Modifier.weight(1f))
                if (isAdminMode) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = GoldLight.copy(0.75f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = Color(0xFFEF5350).copy(0.80f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(technique.localizedDescription(lang), fontSize = 14.sp, color = Color.White.copy(0.82f),
                lineHeight = 20.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (technique.source.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(2.dp).height(14.dp).background(GoldDark.copy(0.70f), RoundedCornerShape(1.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text(technique.source, fontSize = 12.sp, color = GoldDark, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.12f), Color.Transparent))))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TechVoteButton("👍", technique.likes, userVote == "like", Color(0xFF4CAF50), onLike)
                TechVoteButton("👎", technique.dislikes, userVote == "dislike", Color(0xFFEF5350), onDislike)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 13.sp); Spacer(Modifier.width(3.dp))
                    Text("${technique.commentsCount}", color = Color.White.copy(0.50f), fontSize = 13.sp)
                }
                val rating = technique.likes - technique.dislikes
                Box(Modifier.clip(RoundedCornerShape(8.dp))
                    .background(when { rating > 0 -> Color(0xFF4CAF50).copy(0.18f); rating < 0 -> Color(0xFFEF5350).copy(0.18f); else -> Color.White.copy(0.08f) })
                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(if (rating > 0) "+$rating" else "$rating",
                        color = when { rating > 0 -> Color(0xFF4CAF50); rating < 0 -> Color(0xFFEF5350); else -> Color.White.copy(0.50f) },
                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TechVoteButton(emoji: String, count: Int, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeColor.copy(0.18f) else Color.White.copy(0.07f))
            .border(1.dp, if (isActive) activeColor.copy(0.50f) else Color.White.copy(0.10f), RoundedCornerShape(10.dp))
            .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 15.sp); Spacer(Modifier.width(5.dp))
            Text("$count", color = if (isActive) activeColor else Color.White.copy(0.60f),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
        }
    }
}

@Composable
fun VoteButton(emoji: String, count: Int, isActive: Boolean, activeColor: Color, onClick: () -> Unit) =
    TechVoteButton(emoji, count, isActive, activeColor, onClick)

@Composable
fun AdminCodeDialog(onConfirm: (code: String, setError: (String) -> Unit) -> Unit, onDismiss: () -> Unit) {
    var code  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3B1A58), Color(0xFF1A0C30))))
            .border(1.dp, GoldLight.copy(0.35f), RoundedCornerShape(24.dp)).padding(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔐 " + stringResource(R.string.tech_enter_code), fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = GoldLight, fontFamily = PlayfairFamily)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = code,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { code = it; error = null } },
                    label = { Text(stringResource(R.string.tech_access_code), color = Color.White.copy(0.60f)) },
                    isError = error != null, supportingText = { if (error != null) Text(error!!, color = Color.Red) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldLight, unfocusedBorderColor = GoldLight.copy(0.35f),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.08f)).clickable { onDismiss() }.padding(horizontal = 20.dp, vertical = 10.dp)) {
                        Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.75f))
                    }
                    Box(Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
                        .border(1.dp, GoldLight.copy(0.40f), RoundedCornerShape(10.dp))
                        .clickable { onConfirm(code) { msg -> error = msg; code = "" } }.padding(horizontal = 20.dp, vertical = 10.dp)) {
                        Text(stringResource(R.string.btn_login), color = GoldLight, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun TechniqueEditDialog(technique: Technique?, onConfirm: (name: String, description: String, source: String) -> Unit, onDismiss: () -> Unit) {
    var name        by remember { mutableStateOf(technique?.name ?: "") }
    var description by remember { mutableStateOf(technique?.description ?: "") }
    var source      by remember { mutableStateOf(technique?.source ?: "") }
    var nameError   by remember { mutableStateOf(false) }
    var descError   by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3B1A58), Color(0xFF1A0C30))))
            .border(1.dp, GoldLight.copy(0.30f), RoundedCornerShape(24.dp)).padding(24.dp)) {
            Column {
                Text(if (technique != null) stringResource(R.string.tech_edit_title) else stringResource(R.string.tech_add_title),
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldLight, fontFamily = PlayfairFamily)
                Spacer(Modifier.height(16.dp))
                val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldLight, unfocusedBorderColor = GoldLight.copy(0.30f),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                OutlinedTextField(value = name, onValueChange = { name = it; nameError = false },
                    label = { Text(stringResource(R.string.dream_field_name) + " *", color = Color.White.copy(0.60f)) },
                    isError = nameError, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), colors = fieldColors)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it; descError = false },
                    label = { Text(stringResource(R.string.dream_field_desc) + " *", color = Color.White.copy(0.60f)) },
                    isError = descError, modifier = Modifier.fillMaxWidth().height(120.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), colors = fieldColors)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = source, onValueChange = { source = it },
                    label = { Text(stringResource(R.string.tech_source_optional), color = Color.White.copy(0.60f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.tech_source_hint), color = Color.White.copy(0.35f)) }, colors = fieldColors)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.08f)).clickable { onDismiss() }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                        Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.75f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
                        .border(1.dp, GoldLight.copy(0.40f), RoundedCornerShape(10.dp))
                        .clickable { nameError = name.isBlank(); descError = description.isBlank(); if (!nameError && !descError) onConfirm(name, description, source) }
                        .padding(horizontal = 16.dp, vertical = 9.dp)) {
                        Text(stringResource(R.string.btn_save), color = GoldLight, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
