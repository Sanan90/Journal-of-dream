package com.dreamjournal.journalofdream.ui.characters

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import androidx.compose.material.icons.filled.Palette
import com.dreamjournal.journalofdream.ui.common.BackgroundPickerSheet
import com.dreamjournal.journalofdream.ui.common.DreamBackgrounds
import com.dreamjournal.journalofdream.ui.common.DreamBackgroundLayer
import com.dreamjournal.journalofdream.ui.dreams.DreamListItem
import com.dreamjournal.journalofdream.ui.locations.LocAlertDialog
import com.dreamjournal.journalofdream.ui.locations.LocFieldLabel
import com.dreamjournal.journalofdream.ui.locations.LocSaveButton
import com.dreamjournal.journalofdream.ui.locations.LocStatCard
import com.dreamjournal.journalofdream.ui.locations.locFieldColors
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDateShort(date: String): String = try {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
} catch (_: Exception) { date }

private val GoldLightCh = Color(0xFFF0D68C)
private val GoldDarkCh  = Color(0xFFD4A76A)
private val PlayfairFamilyCh = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCharacterScreen(
    navController: NavHostController,
    characterId: Int,
    characterViewModel: CharacterViewModel
) {
    val characterWithDreams by characterViewModel.getCharacterWithDreams(characterId).observeAsState()
    var isEditing        by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameInput  by remember { mutableStateOf("") }
    var descInput  by remember { mutableStateOf("") }
    var nameError  by remember { mutableStateOf(false) }
    var selectedBackgroundId by remember { mutableStateOf(0) }
    var showBackgroundPicker by remember { mutableStateOf(false) }

    characterWithDreams?.let { cwd ->
        val character  = cwd.character
        val dreams     = cwd.dreams
        val dreamCount = dreams.size
        val firstDream = dreams.minByOrNull { it.date }
        val lastDream  = dreams.maxByOrNull { it.date }

        LaunchedEffect(character) {
            if (!isEditing) { nameInput = character.name; descInput = character.description }
            selectedBackgroundId = character.backgroundId
        }

        if (showDeleteDialog) {
            LocAlertDialog(
                title = stringResource(R.string.dialog_delete_character_title),
                message = stringResource(R.string.dialog_delete_character_message, character.name),
                confirmText = stringResource(R.string.btn_delete),
                confirmColor = Color(0xFFEF5350),
                onConfirm = { characterViewModel.deleteCharacter(character); showDeleteDialog = false; navController.popBackStack() },
                onDismiss = { showDeleteDialog = false }
            )
        }

        Box(Modifier.fillMaxSize()) {
            Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            DreamBackgroundLayer(selectedBackgroundId)

            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

                // Заголовок
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (isEditing) { isEditing = false; nameError = false; nameInput = character.name; descInput = character.description }
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GoldLightCh, modifier = Modifier.size(28.dp))
                    }
                    var titleFontSize by remember(character.name, isEditing) { mutableStateOf(26.sp) }
                    Text(
                        text = if (isEditing) stringResource(R.string.character_edit_title) else character.name,
                        color = GoldLightCh, fontFamily = PlayfairFamilyCh, fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (it.didOverflowWidth && titleFontSize > 16.sp) titleFontSize = (titleFontSize.value * 0.9f).sp },
                        modifier = Modifier.weight(1f)
                    )
                    if (isEditing) {
                        IconButton(onClick = { showBackgroundPicker = true }) {
                            Icon(Icons.Default.Palette, null,
                                tint = if (selectedBackgroundId != 0) GoldLightCh else GoldLightCh.copy(0.4f),
                                modifier = Modifier.size(22.dp))
                        }
                    } else {
                        IconButton(onClick = { nameInput = character.name; descInput = character.description; selectedBackgroundId = character.backgroundId; isEditing = true }) {
                            Icon(Icons.Default.Edit, null, tint = GoldLightCh, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(22.dp))
                        }
                    }
                }

                if (!isEditing) {
                    // ── ПРОСМОТР ──────────────────────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                    ) {
                        if (character.description.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF3B1A58).copy(0.72f), Color(0xFF1A0C30).copy(0.82f))))
                                        .border(1.dp, GoldLightCh.copy(0.20f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(character.description, color = Color.White.copy(0.88f), fontSize = 15.sp, lineHeight = 22.sp)
                                }
                            }
                        }

                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LocStatCard(Modifier.weight(1f), stringResource(R.string.location_stat_dreams), "$dreamCount")
                                LocStatCard(Modifier.weight(1.6f), stringResource(R.string.location_stat_first),
                                    if (firstDream != null) formatDateShort(firstDream.date) else "—")
                                LocStatCard(Modifier.weight(1.6f), stringResource(R.string.location_stat_last),
                                    if (lastDream != null) formatDateShort(lastDream.date) else "—")
                            }
                        }

                        item {
                            Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(3.dp).height(20.dp)
                                    .background(Brush.verticalGradient(listOf(GoldLightCh, GoldDarkCh)), RoundedCornerShape(2.dp)))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (dreamCount > 0) stringResource(R.string.character_dreams_title)
                                           else stringResource(R.string.characters_empty_hint),
                                    color = GoldLightCh, fontFamily = PlayfairFamilyCh,
                                    fontWeight = FontWeight.Bold, fontSize = 17.sp
                                )
                            }
                        }

                        if (dreams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF3B1A58).copy(0.45f), Color(0xFF1A0C30).copy(0.55f))))
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🌙", fontSize = 36.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text(stringResource(R.string.character_dreams_empty),
                                            color = Color.White.copy(0.45f), fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(dreams, key = { it.localId }) { dream ->
                                DreamListItem(dream = dream, navController = navController)
                            }
                        }
                    }
                } else {
                    // ── РЕДАКТИРОВАНИЕ ────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .padding(horizontal = 14.dp).navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        LocFieldLabel(R.drawable.dreamers_icon, stringResource(R.string.character_field_name))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it; if (it.isNotBlank()) nameError = false },
                            textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                            singleLine = true, isError = nameError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = locFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        LocFieldLabel(R.drawable.magic_glass_icon, stringResource(R.string.character_field_desc))
                        OutlinedTextField(
                            value = descInput, onValueChange = { descInput = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 22.sp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            shape = RoundedCornerShape(14.dp), colors = locFieldColors()
                        )
                        Spacer(Modifier.height(4.dp))
                        LocSaveButton(
                            text = stringResource(R.string.btn_save),
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                if (nameInput.isBlank()) { nameError = true; return@LocSaveButton }
                                characterViewModel.updateCharacter(character.copy(name = nameInput.trim(), description = descInput.trim(), backgroundId = selectedBackgroundId))
                                isEditing = false
                            }
                        )
                    }
                }
            }
        }
    if (showBackgroundPicker) {
        BackgroundPickerSheet(
            currentBackgroundId = selectedBackgroundId,
            backgroundType = DreamBackgrounds.Type.CHARACTER,
            onBackgroundSelected = { selectedBackgroundId = it; showBackgroundPicker = false },
            onDismiss = { showBackgroundPicker = false }
        )
    }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        CircularProgressIndicator(color = GoldLightCh, strokeWidth = 2.dp)
    }
}
