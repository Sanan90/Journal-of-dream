package com.dreamjournal.journalofdream.ui.dreams

import com.dreamjournal.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.ui.dreams.CategoryManagerDialog
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged

private val GoldLightE      = Color(0xFFF0D68C)
private val GoldDarkE       = Color(0xFFD4A76A)
private val PlayfairFamilyE = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val EditCardBg      = Brush.verticalGradient(
    listOf(Color(0xFF2E1650).copy(0.80f), Color(0xFF160930).copy(0.90f))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditDreamScreen(
    navController: NavHostController,
    dreamId: String,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    characterViewModel: CharacterViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context     = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dreamIdInt  = dreamId.toIntOrNull() ?: 0
    val scrollState = rememberScrollState()

    val dreamWithLocationsState by dreamViewModel.getDreamWithLocationsById(dreamIdInt).observeAsState()
    val allLocations  by locationViewModel.locations.observeAsState(emptyList())
    val allCharacters by characterViewModel.characters.observeAsState(emptyList())
    val categories    by categoryViewModel.allCategories.observeAsState(emptyList())

    dreamWithLocationsState?.let { dreamWithLocs ->
        val dream = dreamWithLocs.dream

        var title    by remember { mutableStateOf(dream.title) }
        var content  by remember { mutableStateOf(dream.content) }
        var selectedCategory       by remember { mutableStateOf<Category?>(null) }
        var categoryMenuExpanded   by remember { mutableStateOf(false) }
        var showDeleteDialog       by remember { mutableStateOf(false) }
        var showTitleError         by remember { mutableStateOf(false) }
        var showExitDialog         by remember { mutableStateOf(false) }
        var showCategoryManager    by remember { mutableStateOf(false) }
        var isLocationDialogOpen   by remember { mutableStateOf(false) }
        var isCharacterDialogOpen  by remember { mutableStateOf(false) }
        val selectedLocationIds  = remember { mutableStateListOf<Int>() }
        val selectedCharacterIds = remember { mutableStateListOf<Int>() }

        // Быстрое создание локации прямо из диалога
        var showQuickAddLocation   by remember { mutableStateOf(false) }
        var quickLocationName      by remember { mutableStateOf("") }
        var quickLocationDesc      by remember { mutableStateOf("") }
        var showQuickLocationDesc  by remember { mutableStateOf(false) }

        // Быстрое создание образа прямо из диалога
        var showQuickAddCharacter  by remember { mutableStateOf(false) }
        var quickCharacterName     by remember { mutableStateOf("") }
        var quickCharacterDesc     by remember { mutableStateOf("") }
        var showQuickCharacterDesc by remember { mutableStateOf(false) }
        var selectedTime  by remember { mutableStateOf(dream.time.ifBlank { getCurrentTime() }) }
        var showTimePicker by remember { mutableStateOf(false) }

        val hasUnsavedChanges = title != dream.title || content != dream.content
        BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

        LaunchedEffect(categories) {
            if (selectedCategory == null && categories.isNotEmpty())
                selectedCategory = categories.find { it.name == dream.category }
        }
        LaunchedEffect(dreamWithLocs) {
            selectedLocationIds.clear()
            selectedLocationIds.addAll(dreamWithLocs.locations.map { it.id })
            selectedCharacterIds.clear()
            selectedCharacterIds.addAll(dreamWithLocs.characters.map { it.id })
        }

        if (showExitDialog) {
            DreamAlertDialog(
                title = stringResource(R.string.discard_title),
                message = stringResource(R.string.discard_dream_message),
                confirmText = stringResource(R.string.discard_confirm),
                confirmColor = Color(0xFFEF5350),
                onConfirm = { showExitDialog = false; navController.popBackStack() },
                onDismiss = { showExitDialog = false }
            )
        }
        if (showDeleteDialog) {
            DreamAlertDialog(
                title = stringResource(R.string.dialog_delete_dream_title),
                message = "\"${dream.title}\" ${stringResource(R.string.dialog_delete_dream_message)}",
                confirmText = stringResource(R.string.btn_delete),
                confirmColor = Color(0xFFEF5350),
                onConfirm = { showDeleteDialog = false; dreamViewModel.deleteDream(dream); navController.popBackStack() },
                onDismiss = { showDeleteDialog = false }
            )
        }
        if (showTimePicker) {
            val timeParts = selectedTime.split(":").map { it.toIntOrNull() ?: 0 }
            android.app.TimePickerDialog(context, { _, h, m ->
                selectedTime = String.format("%02d:%02d", h, m); showTimePicker = false
            }, timeParts.getOrElse(0) { 0 }, timeParts.getOrElse(1) { 0 }, true)
                .also { it.setOnCancelListener { showTimePicker = false }; it.show() }
        }
        if (isLocationDialogOpen) {
            if (showQuickAddLocation) {
                AlertDialog(
                    onDismissRequest = {
                        showQuickAddLocation = false
                        quickLocationName = ""; quickLocationDesc = ""; showQuickLocationDesc = false
                    },
                    title = { Text(stringResource(R.string.location_add_title), color = GoldLightE, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = quickLocationName,
                                onValueChange = { quickLocationName = it },
                                placeholder = { Text(stringResource(R.string.location_field_name), color = Color.White.copy(0.4f)) },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White),
                                colors = editFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showQuickLocationDesc) {
                                OutlinedTextField(
                                    value = quickLocationDesc,
                                    onValueChange = { quickLocationDesc = it },
                                    placeholder = { Text(stringResource(R.string.location_field_desc), color = Color.White.copy(0.4f)) },
                                    textStyle = TextStyle(color = Color.White),
                                    colors = editFieldColors(),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                                )
                            } else {
                                TextButton(onClick = { showQuickLocationDesc = true }, modifier = Modifier.padding(0.dp)) {
                                    Text("+ ${stringResource(R.string.location_field_desc)}", color = GoldLightE.copy(0.75f), fontSize = 13.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (quickLocationName.isNotBlank()) {
                                    locationViewModel.addLocation(quickLocationName.trim(), quickLocationDesc.trim())
                                    quickLocationName = ""; quickLocationDesc = ""
                                    showQuickLocationDesc = false; showQuickAddLocation = false
                                }
                            },
                            enabled = quickLocationName.isNotBlank()
                        ) { Text(stringResource(R.string.btn_save), color = GoldLightE, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showQuickAddLocation = false; quickLocationName = ""
                            quickLocationDesc = ""; showQuickLocationDesc = false
                        }) { Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.6f)) }
                    },
                    containerColor = Color(0xFF2A1548)
                )
            } else {
                AlertDialog(
                    onDismissRequest = { isLocationDialogOpen = false },
                    title = { Text(stringResource(R.string.dream_pick_locations), color = GoldLightE, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            TextButton(onClick = { showQuickAddLocation = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("+ ${stringResource(R.string.location_add_title)}", color = GoldLightE, fontSize = 13.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.1f)))
                            LazyColumn {
                                items(allLocations) { loc ->
                                    val sel = selectedLocationIds.contains(loc.id)
                                    DreamPickerRow(loc.name, sel) {
                                        if (sel) selectedLocationIds.remove(loc.id) else selectedLocationIds.add(loc.id)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { isLocationDialogOpen = false }) {
                            Text(stringResource(R.string.ok), color = GoldLightE, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFF2A1548)
                )
            }
        }

        if (isCharacterDialogOpen) {
            if (showQuickAddCharacter) {
                AlertDialog(
                    onDismissRequest = {
                        showQuickAddCharacter = false
                        quickCharacterName = ""; quickCharacterDesc = ""; showQuickCharacterDesc = false
                    },
                    title = { Text(stringResource(R.string.character_add_title), color = GoldLightE, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = quickCharacterName,
                                onValueChange = { quickCharacterName = it },
                                placeholder = { Text(stringResource(R.string.character_field_name), color = Color.White.copy(0.4f)) },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White),
                                colors = editFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showQuickCharacterDesc) {
                                OutlinedTextField(
                                    value = quickCharacterDesc,
                                    onValueChange = { quickCharacterDesc = it },
                                    placeholder = { Text(stringResource(R.string.character_field_desc), color = Color.White.copy(0.4f)) },
                                    textStyle = TextStyle(color = Color.White),
                                    colors = editFieldColors(),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                                )
                            } else {
                                TextButton(onClick = { showQuickCharacterDesc = true }, modifier = Modifier.padding(0.dp)) {
                                    Text("+ ${stringResource(R.string.character_field_desc)}", color = GoldLightE.copy(0.75f), fontSize = 13.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (quickCharacterName.isNotBlank()) {
                                    characterViewModel.addCharacter(quickCharacterName.trim(), quickCharacterDesc.trim())
                                    quickCharacterName = ""; quickCharacterDesc = ""
                                    showQuickCharacterDesc = false; showQuickAddCharacter = false
                                }
                            },
                            enabled = quickCharacterName.isNotBlank()
                        ) { Text(stringResource(R.string.btn_save), color = GoldLightE, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showQuickAddCharacter = false; quickCharacterName = ""
                            quickCharacterDesc = ""; showQuickCharacterDesc = false
                        }) { Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.6f)) }
                    },
                    containerColor = Color(0xFF2A1548)
                )
            } else {
                AlertDialog(
                    onDismissRequest = { isCharacterDialogOpen = false },
                    title = { Text(stringResource(R.string.dream_pick_characters), color = GoldLightE, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            TextButton(onClick = { showQuickAddCharacter = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("+ ${stringResource(R.string.character_add_title)}", color = GoldLightE, fontSize = 13.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.1f)))
                            LazyColumn {
                                items(allCharacters) { ch ->
                                    val sel = selectedCharacterIds.contains(ch.id)
                                    DreamPickerRow(ch.name, sel) {
                                        if (sel) selectedCharacterIds.remove(ch.id) else selectedCharacterIds.add(ch.id)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { isCharacterDialogOpen = false }) {
                            Text(stringResource(R.string.ok), color = GoldLightE, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFF2A1548)
                )
            }
        }
        if (showCategoryManager) {
            CategoryManagerDialog(
                categoryViewModel = categoryViewModel,
                onDismiss = { showCategoryManager = false },
                onCategorySelected = { selectedCategory = it }
            )
        }

        Box(Modifier.fillMaxSize()) {
            Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {

                // ── Заголовок ──
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (hasUnsavedChanges) showExitDialog = true else navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GoldLightE, modifier = Modifier.size(28.dp))
                    }
                    Text(stringResource(R.string.dream_edit_title), color = GoldLightE,
                        fontFamily = PlayfairFamilyE, fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, modifier = Modifier.weight(1f))
                    // Кнопка удаления
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF4A1020).copy(0.80f), Color(0xFF1A0808).copy(0.90f))))
                            .border(1.dp, Color(0xFFEF5350).copy(0.45f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            focusManager.clearFocus(); keyboardController?.hide()
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(Modifier.height(2.dp))

                    // 1. Название
                    DreamSectionLabel(R.drawable.settings_quotes, stringResource(R.string.dream_field_name))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; if (it.isNotBlank()) showTitleError = false },
                        textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                        singleLine = true,
                        isError = showTitleError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors()
                    )

                    // 2. Дата (не редактируемая) / Время
                    DreamSectionLabel(R.drawable.dream_view_date, stringResource(R.string.dream_label_date))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Дата — заблокирована
                        Row(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(EditCardBg)
                                .border(1.dp, GoldLightE.copy(0.12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(painterResource(R.drawable.dream_view_date), null,
                                Modifier.size(15.dp).then(Modifier), contentScale = ContentScale.Fit, alpha = 0.40f)
                            Spacer(Modifier.width(6.dp))
                            Text(formatDateForDisplay(dream.date), color = Color.White.copy(0.45f), fontSize = 12.sp)
                        }
                        DreamTapChip(Modifier.weight(0.65f), R.drawable.dream_view_time, selectedTime) { showTimePicker = true }
                    }

                    // 3. Описание
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DreamTapChip(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.location_icon_gold,
                            text = if (selectedLocationIds.isEmpty()) stringResource(R.string.dream_label_locations)
                                   else stringResource(R.string.dream_locations_selected, selectedLocationIds.size),
                            onClick = { isLocationDialogOpen = true }
                        )
                        DreamTapChip(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.dreamers_icon,
                            text = if (selectedCharacterIds.isEmpty()) stringResource(R.string.dream_label_characters)
                                   else stringResource(R.string.dream_characters_selected, selectedCharacterIds.size),
                            onClick = { isCharacterDialogOpen = true }
                        )
                    }

                    DreamSectionLabel(R.drawable.dream_field_content, stringResource(R.string.dream_field_desc))
                    val contentBringIntoView = remember { BringIntoViewRequester() }
                    var contentFocused by remember { mutableStateOf(false) }
                    LaunchedEffect(content, contentFocused) {
                        if (contentFocused) contentBringIntoView.bringIntoView()
                    }
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 24.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 420.dp)
                            .bringIntoViewRequester(contentBringIntoView)
                            .onFocusChanged { contentFocused = it.isFocused },
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors(),
                        singleLine = false,
                        maxLines = Int.MAX_VALUE
                    )

                    // 4. Категория
                    DreamSectionLabel(R.drawable.dream_view_category, stringResource(R.string.dream_label_category))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                .background(EditCardBg)
                                .border(1.dp, GoldLightE.copy(0.22f), RoundedCornerShape(14.dp))
                                .clickable { categoryMenuExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Text(selectedCategory?.name?.let {
                                localizeCategory(it, stringResource(R.string.dreams_no_category),
                                    stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid),
                                    stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))
                            } ?: stringResource(R.string.dreams_no_category), color = Color.White, fontSize = 16.sp)
                            DropdownMenu(categoryMenuExpanded, { categoryMenuExpanded = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.dreams_no_category)) },
                                    onClick = { selectedCategory = null; categoryMenuExpanded = false })
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(localizeCategory(cat.name, stringResource(R.string.dreams_no_category),
                                            stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid),
                                            stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))) },
                                        onClick = { selectedCategory = cat; categoryMenuExpanded = false })
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(EditCardBg).border(1.dp, GoldLightE.copy(0.22f), RoundedCornerShape(12.dp))
                                .clickable { showCategoryManager = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = GoldLightE, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Кнопка сохранения
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.align(Alignment.End)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
                            .border(1.dp, GoldLightE.copy(0.50f), RoundedCornerShape(16.dp))
                            .clickable {
                                if (title.isBlank()) { showTitleError = true; return@clickable }
                                dreamViewModel.updateDream(
                                    dream.copy(title = title.trim(), content = content.trim(),
                                        time = selectedTime, category = selectedCategory?.name ?: context.getString(R.string.dreams_no_category)),
                                    selectedLocationIds.toList(), selectedCharacterIds.toList()
                                )
                                keyboardController?.hide()
                                navController.popBackStack()
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(painterResource(R.drawable.dream_save_btn), null, Modifier.size(20.dp), contentScale = ContentScale.Fit)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.dream_save_changes), color = GoldLightE,
                                fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    } ?: Box(Modifier.fillMaxSize())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GoldLightE.copy(0.65f),
    unfocusedBorderColor    = GoldLightE.copy(0.22f),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    cursorColor             = GoldLightE,
    focusedContainerColor   = Color(0xFF2E1650).copy(0.80f),
    unfocusedContainerColor = Color(0xFF160930).copy(0.90f),
    focusedPlaceholderColor   = Color.White.copy(0.35f),
    unfocusedPlaceholderColor = Color.White.copy(0.35f)
)
