package com.dreamjournal.journalofdream.ui.dreams

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import com.dreamjournal.journalofdream.util.localizeCategory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.util.rememberSpeechLauncher
import com.dreamjournal.journalofdream.ui.dreams.CategoryManagerDialog
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

fun getCurrentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

fun formatDateForDisplay(date: String): String = try {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
} catch (e: Exception) { date }

fun formatDateForTitle(date: String, context: android.content.Context): String = try {
    val langCode = com.dreamjournal.journalofdream.util.LocaleHelper.getSavedLanguage(context)
    val locale = if (langCode == "system") Locale.getDefault() else Locale(langCode)
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!
    context.getString(R.string.dream_auto_title,
        SimpleDateFormat("d", locale).format(parsed),
        SimpleDateFormat("MMMM", locale).format(parsed))
} catch (e: Exception) { date }

private val GoldLight      = Color(0xFFF0D68C)
private val GoldDark       = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val DreamCardBg    = Brush.verticalGradient(
    listOf(Color(0xFF2E1650).copy(0.80f), Color(0xFF160930).copy(0.90f))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddDreamScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    characterViewModel: CharacterViewModel = viewModel()
) {
    val context            = LocalContext.current
    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val descriptionBringIntoViewRequester = remember { BringIntoViewRequester() }

    var title   by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory       by remember { mutableStateOf<Category?>(null) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var showExitDialog         by remember { mutableStateOf(false) }
    var selectedDate  by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTime  by remember { mutableStateOf(getCurrentTime()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryManager   by remember { mutableStateOf(false) }
    var isLocationDialogOpen  by remember { mutableStateOf(false) }
    var isCharacterDialogOpen by remember { mutableStateOf(false) }
    val selectedLocationIds  = remember { mutableStateListOf<Int>() }
    val selectedCharacterIds = remember { mutableStateListOf<Int>() }

    val categoryViewModel: CategoryViewModel = viewModel()
    val categories    by categoryViewModel.allCategories.observeAsState(listOf())
    val allLocations  by locationViewModel.locations.observeAsState(emptyList())
    val allCharacters by characterViewModel.characters.observeAsState(emptyList())
    val speechLauncher = rememberSpeechLauncher { recognized ->
        content = if (content.isBlank()) recognized else "$content $recognized"
    }

    val hasUnsavedChanges = title.isNotBlank() || content.isNotBlank()
    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

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
    if (showDatePicker) {
        val cal = Calendar.getInstance()
        android.app.DatePickerDialog(context, { _, y, m, d ->
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            showDatePicker = false
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            .also { it.setOnCancelListener { showDatePicker = false }; it.show() }
    }
    if (showTimePicker) {
        val cal = Calendar.getInstance()
        android.app.TimePickerDialog(context, { _, h, m ->
            selectedTime = String.format("%02d:%02d", h, m)
            showTimePicker = false
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
            .also { it.setOnCancelListener { showTimePicker = false }; it.show() }
    }
    if (isLocationDialogOpen) {
        DreamPickerDialog(stringResource(R.string.dream_pick_locations), { isLocationDialogOpen = false }) {
            items(allLocations) { loc ->
                val sel = selectedLocationIds.contains(loc.id)
                DreamPickerRow(loc.name, sel) { if (sel) selectedLocationIds.remove(loc.id) else selectedLocationIds.add(loc.id) }
            }
        }
    }
    if (isCharacterDialogOpen) {
        DreamPickerDialog(stringResource(R.string.dream_pick_characters), { isCharacterDialogOpen = false }) {
            items(allCharacters) { ch ->
                val sel = selectedCharacterIds.contains(ch.id)
                DreamPickerRow(ch.name, sel) { if (sel) selectedCharacterIds.remove(ch.id) else selectedCharacterIds.add(ch.id) }
            }
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
                    Icon(Icons.Default.ArrowBack, null, tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.dream_add_title), color = GoldLight,
                    fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.dream_field_content), null,
                    Modifier.size(32.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
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
                    onValueChange = { title = it },
                    placeholder = { Text(formatDateForTitle(selectedDate, context), color = Color.White.copy(0.35f), fontSize = 17.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = dreamFieldColors()
                )

                // 2. Дата / Время
                DreamSectionLabel(R.drawable.dream_view_date, stringResource(R.string.dream_label_date))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DreamTapChip(Modifier.weight(1f), R.drawable.dream_view_date, formatDateForDisplay(selectedDate)) { showDatePicker = true }
                    DreamTapChip(Modifier.weight(0.65f), R.drawable.dream_view_time, selectedTime) { showTimePicker = true }
                }

                // 3. Описание сна (с кнопками локаций/образов над полем)
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it
                            coroutineScope.launch {
                                delay(100)
                                scrollState.scrollTo(scrollState.value + 60)
                            }
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 24.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        modifier = Modifier.weight(1f).heightIn(min = 160.dp)
                            .bringIntoViewRequester(descriptionBringIntoViewRequester),
                        shape = RoundedCornerShape(14.dp),
                        colors = dreamFieldColors()
                    )
                    Box(
                        modifier = Modifier.padding(top = 2.dp).size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF7B3FA0).copy(0.90f), Color(0xFF4A2870).copy(0.90f))))
                            .border(1.dp, GoldLight.copy(0.40f), RoundedCornerShape(12.dp))
                            .clickable { speechLauncher() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(painterResource(R.drawable.dream_mic_btn), null, Modifier.size(26.dp), contentScale = ContentScale.Fit)
                    }
                }

                // 4. Категория
                DreamSectionLabel(R.drawable.dream_view_category, stringResource(R.string.dream_label_category))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(DreamCardBg)
                            .border(1.dp, GoldLight.copy(0.22f), RoundedCornerShape(14.dp))
                            .clickable { isCategoryMenuExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(selectedCategory?.name?.let {
                            localizeCategory(it, stringResource(R.string.dreams_no_category),
                                stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid),
                                stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))
                        } ?: stringResource(R.string.dreams_no_category), color = Color.White, fontSize = 16.sp)
                        DropdownMenu(isCategoryMenuExpanded, { isCategoryMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.dreams_no_category)) },
                                onClick = { selectedCategory = null; isCategoryMenuExpanded = false })
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(localizeCategory(cat.name, stringResource(R.string.dreams_no_category),
                                        stringResource(R.string.cat_nightmares), stringResource(R.string.cat_lucid),
                                        stringResource(R.string.cat_plot), stringResource(R.string.cat_personal))) },
                                    onClick = { selectedCategory = cat; isCategoryMenuExpanded = false })
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(DreamCardBg).border(1.dp, GoldLight.copy(0.22f), RoundedCornerShape(12.dp))
                            .clickable { showCategoryManager = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = GoldLight, modifier = Modifier.size(20.dp))
                    }
                }

                // Кнопка сохранения
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier.align(Alignment.End)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
                        .border(1.dp, GoldLight.copy(0.50f), RoundedCornerShape(16.dp))
                        .clickable {
                            val finalTitle = if (title.isBlank()) formatDateForTitle(selectedDate, context) else title.trim()
                            dreamViewModel.addDream(
                                Dream(title = finalTitle, content = content.trim(), date = selectedDate,
                                    time = selectedTime, category = selectedCategory?.name ?: context.getString(R.string.dreams_no_category)),
                                selectedLocationIds.toList(), selectedCharacterIds.toList()
                            )
                            navController.popBackStack()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.dream_save_btn), null, Modifier.size(20.dp), contentScale = ContentScale.Fit)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_save), color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── Переиспользуемые компоненты ──────────────────────────────────────────────

@Composable
fun DreamSectionLabel(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(iconRes), null, Modifier.size(14.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(5.dp))
        Text(text, color = GoldLight.copy(0.80f), fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    }
}

@Composable
fun DreamInputCard(modifier: Modifier = Modifier.fillMaxWidth(), content: @Composable () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp))
            .background(DreamCardBg)
            .border(1.dp, GoldLight.copy(0.22f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) { content() }
}

@Composable
fun DreamTapChip(modifier: Modifier = Modifier, iconRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(DreamCardBg)
            .border(1.dp, GoldLight.copy(0.20f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(iconRes), null, Modifier.size(15.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White.copy(0.88f), fontSize = 12.sp,
            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun DreamAlertDialog(
    title: String, message: String, confirmText: String,
    confirmColor: Color = GoldLight, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = GoldLight, fontWeight = FontWeight.Bold) },
        text  = { Text(message, color = Color.White.copy(0.85f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = confirmColor, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.discard_dismiss), color = Color.White.copy(0.70f)) } },
        containerColor = Color(0xFF2A1548)
    )
}

@Composable
fun DreamPickerDialog(title: String, onDismiss: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = GoldLight, fontWeight = FontWeight.Bold) },
        text  = { LazyColumn { content() } },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok), color = GoldLight, fontWeight = FontWeight.SemiBold) } },
        containerColor = Color(0xFF2A1548)
    )
}

@Composable
fun DreamPickerRow(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GoldLight.copy(0.12f) else Color.Transparent)
            .clickable { onToggle() }.padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = GoldLight,
                uncheckedColor = Color.White.copy(0.45f), checkmarkColor = Color(0xFF1A0C30)))
        Spacer(Modifier.width(6.dp))
        Text(name, color = if (isSelected) GoldLight else Color.White, fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dreamFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GoldLight.copy(0.65f),
    unfocusedBorderColor    = GoldLight.copy(0.22f),
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White,
    cursorColor             = GoldLight,
    focusedContainerColor   = Color(0xFF2E1650).copy(0.80f),
    unfocusedContainerColor = Color(0xFF160930).copy(0.90f),
    focusedPlaceholderColor   = Color.White.copy(0.35f),
    unfocusedPlaceholderColor = Color.White.copy(0.35f)
)

// Алиасы
@Composable fun SmallLabel(iconRes: Int, text: String) = DreamSectionLabel(iconRes, text)
@Composable fun DreamInputBox(modifier: Modifier = Modifier.fillMaxWidth(), content: @Composable () -> Unit) = DreamInputCard(modifier, content)
@Composable fun DreamSmallChip(modifier: Modifier = Modifier, iconRes: Int, text: String, onClick: () -> Unit) = DreamTapChip(modifier, iconRes, text, onClick)
@Composable fun DreamFieldLabel(iconRes: Int, text: String) = DreamSectionLabel(iconRes, text)
@Composable fun DreamChipButton(modifier: Modifier = Modifier, iconRes: Int, text: String, onClick: () -> Unit) = DreamTapChip(modifier, iconRes, text, onClick)
