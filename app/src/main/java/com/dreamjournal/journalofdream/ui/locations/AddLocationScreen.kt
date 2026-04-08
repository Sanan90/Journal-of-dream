package com.dreamjournal.journalofdream.ui.locations

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
import androidx.compose.ui.draw.clip
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
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark  = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val LocFieldBg = Brush.verticalGradient(
    listOf(Color(0xFF2E1650).copy(0.80f), Color(0xFF160930).copy(0.90f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    var locationName        by remember { mutableStateOf("") }
    var locationDescription by remember { mutableStateOf("") }
    var showError      by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedBackgroundId by remember { mutableStateOf(0) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasUnsavedChanges = locationName.isNotBlank() || locationDescription.isNotBlank()

    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        LocAlertDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_location_message),
            confirmText = stringResource(R.string.discard_confirm),
            confirmColor = Color(0xFFEF5350),
            onConfirm = { showExitDialog = false; navController.popBackStack() },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showBackgroundPicker) {
        BackgroundPickerSheet(
            currentBackgroundId = selectedBackgroundId,
            backgroundType = DreamBackgrounds.Type.LOCATION,
            onBackgroundSelected = { selectedBackgroundId = it; showBackgroundPicker = false },
            onDismiss = { showBackgroundPicker = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Фон локации поверх
        DreamBackgroundLayer(selectedBackgroundId)

        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            // Заголовок
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (hasUnsavedChanges) showExitDialog = true else navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.location_add_title), color = GoldLight,
                    fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, modifier = Modifier.weight(1f))
                // Кнопка выбора фона
                IconButton(onClick = { showBackgroundPicker = true }) {
                    Icon(Icons.Default.Palette, "Фон",
                        tint = if (selectedBackgroundId != 0) GoldLight else GoldLight.copy(0.4f),
                        modifier = Modifier.size(24.dp))
                }
                Image(painterResource(R.drawable.location_icon_gold), null,
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

                LocFieldLabel(R.drawable.location_icon_gold, stringResource(R.string.location_field_name))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it; if (it.isNotBlank()) showError = false },
                    placeholder = { Text(stringResource(R.string.location_field_name), color = Color.White.copy(0.35f)) },
                    textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                    singleLine = true, isError = showError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = locFieldColors()
                )
                if (showError) Text(stringResource(R.string.location_error_name),
                    color = Color(0xFFEF5350), fontSize = 11.sp)

                LocFieldLabel(R.drawable.location_map_icon, stringResource(R.string.location_field_desc))
                OutlinedTextField(
                    value = locationDescription,
                    onValueChange = { locationDescription = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 22.sp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = locFieldColors()
                )

                Spacer(Modifier.height(4.dp))
                LocSaveButton(
                    text = stringResource(R.string.btn_save),
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        if (locationName.isBlank()) { showError = true; return@LocSaveButton }
                        locationViewModel.addLocation(
                            locationName.trim(),
                            locationDescription.trim(),
                            selectedBackgroundId
                        )
                        keyboardController?.hide()
                        navController.popBackStack()
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── Переиспользуемые компоненты ──────────────────────────────────────────────

@Composable
fun LocFieldLabel(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(iconRes), null, Modifier.size(14.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(5.dp))
        Text(text, color = GoldLight.copy(0.80f), fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    }
}

@Composable
fun LocSaveButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
            .border(1.dp, GoldLight.copy(0.50f), RoundedCornerShape(16.dp))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(text, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun LocAlertDialog(
    title: String, message: String,
    confirmText: String, confirmColor: Color = GoldLight,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(title, color = GoldLight, fontWeight = FontWeight.Bold) },
        text   = { Text(message, color = Color.White.copy(0.85f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = confirmColor, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.70f)) } },
        containerColor = Color(0xFF2A1548)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun locFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GoldLight.copy(0.65f),
    unfocusedBorderColor    = GoldLight.copy(0.22f),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    cursorColor             = GoldLight,
    focusedContainerColor   = Color(0xFF2E1650).copy(0.80f),
    unfocusedContainerColor = Color(0xFF160930).copy(0.90f),
    focusedPlaceholderColor   = Color.White.copy(0.35f),
    unfocusedPlaceholderColor = Color.White.copy(0.35f),
    focusedLabelColor   = GoldLight,
    unfocusedLabelColor = Color.White.copy(0.55f)
)

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
