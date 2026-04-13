package com.dreamjournal.journalofdream.ui.characters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.common.BackgroundPickerSheet
import com.dreamjournal.journalofdream.ui.common.DreamBackgrounds
import com.dreamjournal.journalofdream.ui.locations.LocAlertDialog
import com.dreamjournal.journalofdream.ui.locations.LocFieldLabel
import com.dreamjournal.journalofdream.ui.locations.LocSaveButton
import com.dreamjournal.journalofdream.ui.locations.locFieldColors
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch

private val GoldLightC = Color(0xFFF0D68C)
private val PlayfairFamilyC = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddCharacterScreen(
    navController: NavHostController,
    characterViewModel: CharacterViewModel
) {
    var name        by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError      by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedBackgroundId by remember { mutableStateOf(0) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasUnsavedChanges = name.isNotBlank() || description.isNotBlank()

    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        LocAlertDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_character_message),
            confirmText = stringResource(R.string.discard_confirm),
            confirmColor = Color(0xFFEF5350),
            onConfirm = { showExitDialog = false; navController.popBackStack() },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showBackgroundPicker) {
        BackgroundPickerSheet(
            currentBackgroundId = selectedBackgroundId,
            backgroundType = DreamBackgrounds.Type.CHARACTER,
            onBackgroundSelected = { selectedBackgroundId = it; showBackgroundPicker = false },
            onDismiss = { showBackgroundPicker = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Фон образа поверх
        DreamBackgroundLayer(selectedBackgroundId)

        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            // Заголовок
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (hasUnsavedChanges) showExitDialog = true else navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = GoldLightC, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.character_add_title), color = GoldLightC,
                    fontFamily = PlayfairFamilyC, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, modifier = Modifier.weight(1f))
                // Кнопка выбора фона
                IconButton(onClick = { showBackgroundPicker = true }) {
                    Icon(Icons.Default.Palette, "Фон",
                        tint = if (selectedBackgroundId != 0) GoldLightC else GoldLightC.copy(0.4f),
                        modifier = Modifier.size(24.dp))
                }
                Image(painterResource(R.drawable.dreamers_icon), null,
                    Modifier.size(32.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                LocFieldLabel(R.drawable.dreamers_icon, stringResource(R.string.character_field_name))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (it.isNotBlank()) showError = false },
                    placeholder = { Text(stringResource(R.string.character_field_name), color = Color.White.copy(0.35f)) },
                    textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                    singleLine = true, isError = showError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = locFieldColors()
                )
                if (showError) Text(stringResource(R.string.character_error_name),
                    color = Color(0xFFEF5350), fontSize = 11.sp)

                LocFieldLabel(R.drawable.magic_glass_icon, stringResource(R.string.character_field_desc))
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                var descFieldFocused by remember { mutableStateOf(false) }
                LaunchedEffect(description, descFieldFocused) {
                    if (descFieldFocused) bringIntoViewRequester.bringIntoView()
                }
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 22.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 420.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusChanged { descFieldFocused = it.isFocused },
                    shape = RoundedCornerShape(14.dp),
                    colors = locFieldColors(),
                    singleLine = false,
                    minLines = 5,
                    maxLines = Int.MAX_VALUE,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )

                Spacer(Modifier.height(4.dp))
                LocSaveButton(
                    text = stringResource(R.string.btn_save),
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        if (name.isBlank()) { showError = true; return@LocSaveButton }
                        characterViewModel.addCharacter(name.trim(), description.trim(), selectedBackgroundId)
                        keyboardController?.hide()
                        navController.popBackStack()
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
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
