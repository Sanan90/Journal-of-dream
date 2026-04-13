package com.dreamjournal.journalofdream.ui.locations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.common.BackgroundPickerSheet
import com.dreamjournal.journalofdream.ui.common.DreamBackgroundLayer
import com.dreamjournal.journalofdream.ui.common.DreamBackgrounds
import com.dreamjournal.journalofdream.ui.dreams.DreamListItem
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDateShort(date: String): String = try {
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!)
} catch (e: Exception) {
    date
}

private val GoldLightV = Color(0xFFF0D68C)
private val GoldDarkV = Color(0xFFD4A76A)
private val PlayfairFamilyV = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val locationWithDreamsState by locationViewModel.getLocationWithDreams(locationId).observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var selectedBackgroundId by remember { mutableStateOf(0) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    locationWithDreamsState?.let { locWithDreams ->
        val location = locWithDreams.location
        val dreams = locWithDreams.dreams
        val dreamCount = dreams.size
        val firstDream = dreams.minByOrNull { it.date }
        val lastDream = dreams.maxByOrNull { it.date }

        LaunchedEffect(location) {
            if (!isEditing) {
                nameInput = location.name
                descInput = location.description
            }
            selectedBackgroundId = location.backgroundId
        }

        val hasUnsavedChanges = isEditing && (
            nameInput != location.name ||
                descInput != location.description ||
                selectedBackgroundId != location.backgroundId
            )

        BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

        if (showExitDialog) {
            LocAlertDialog(
                title = stringResource(R.string.discard_title),
                message = stringResource(R.string.discard_dream_message),
                confirmText = stringResource(R.string.discard_confirm),
                confirmColor = Color(0xFFEF5350),
                onConfirm = {
                    showExitDialog = false
                    isEditing = false
                    nameError = false
                    nameInput = location.name
                    descInput = location.description
                    selectedBackgroundId = location.backgroundId
                },
                onDismiss = { showExitDialog = false }
            )
        }

        if (showDeleteDialog) {
            LocAlertDialog(
                title = stringResource(R.string.dialog_delete_location_title),
                message = "\"${location.name}\"" + if (dreamCount > 0) {
                    "\n\n$dreamCount ${dreamWord(dreamCount)} останутся, но привязка исчезнет."
                } else {
                    ""
                },
                confirmText = stringResource(R.string.btn_delete),
                confirmColor = Color(0xFFEF5350),
                onConfirm = {
                    locationViewModel.deleteLocation(location)
                    navController.popBackStack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.new_fon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            DreamBackgroundLayer(selectedBackgroundId)

            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF12071F).copy(alpha = 0.96f),
                                    Color(0xFF12071F).copy(alpha = 0.88f)
                                )
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .zIndex(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        when {
                            hasUnsavedChanges -> showExitDialog = true
                            isEditing -> {
                                isEditing = false
                                nameError = false
                                nameInput = location.name
                                descInput = location.description
                                selectedBackgroundId = location.backgroundId
                            }
                            else -> navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = GoldLightV,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    var titleFontSize by remember(location.name, isEditing) { mutableStateOf(26.sp) }
                    Text(
                        text = if (isEditing) stringResource(R.string.location_edit_title) else location.name,
                        color = GoldLightV,
                        fontFamily = PlayfairFamilyV,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            if (it.didOverflowWidth && titleFontSize > 16.sp) {
                                titleFontSize = (titleFontSize.value * 0.9f).sp
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (isEditing) {
                        IconButton(onClick = { showBackgroundPicker = true }) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (selectedBackgroundId != 0) GoldLightV else GoldLightV.copy(0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            nameInput = location.name
                            descInput = location.description
                            selectedBackgroundId = location.backgroundId
                            isEditing = true
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = GoldLightV,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (!isEditing) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                    ) {
                        if (location.description.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF3B1A58).copy(0.72f),
                                                    Color(0xFF1A0C30).copy(0.82f)
                                                )
                                            )
                                        )
                                        .border(1.dp, GoldLightV.copy(0.20f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = location.description,
                                        color = Color.White.copy(0.88f),
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LocStatCard(
                                    Modifier.weight(1f),
                                    stringResource(R.string.location_stat_dreams),
                                    "$dreamCount"
                                )
                                LocStatCard(
                                    Modifier.weight(1.6f),
                                    stringResource(R.string.location_stat_first),
                                    if (firstDream != null) formatDateShort(firstDream.date) else "—"
                                )
                                LocStatCard(
                                    Modifier.weight(1.6f),
                                    stringResource(R.string.location_stat_last),
                                    if (lastDream != null) formatDateShort(lastDream.date) else "—"
                                )
                            }
                        }

                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(
                                            Brush.verticalGradient(listOf(GoldLightV, GoldDarkV)),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (dreamCount > 0) {
                                        stringResource(R.string.location_dreams_title)
                                    } else {
                                        stringResource(R.string.locations_no_dreams)
                                    },
                                    color = GoldLightV,
                                    fontFamily = PlayfairFamilyV,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        if (dreams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF3B1A58).copy(0.45f),
                                                    Color(0xFF1A0C30).copy(0.55f)
                                                )
                                            )
                                        )
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🌙", fontSize = 36.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.location_dreams_empty_hint),
                                            color = Color.White.copy(0.45f),
                                            fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
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
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    var descFieldFocused by remember { mutableStateOf(false) }

                    LaunchedEffect(descInput, descFieldFocused) {
                        if (descFieldFocused) {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 14.dp)
                            .imePadding()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
                    ) {
                        item {
                            LocFieldLabel(
                                R.drawable.location_icon_gold,
                                stringResource(R.string.location_field_name)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    if (it.isNotBlank()) nameError = false
                                },
                                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                                singleLine = true,
                                isError = nameError,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = locFieldColors(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                        }

                        item {
                            LocFieldLabel(
                                R.drawable.location_map_icon,
                                stringResource(R.string.location_field_desc)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = descInput,
                                onValueChange = { descInput = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                ),
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
                        }

                        item {
                            Spacer(Modifier.height(4.dp))
                        }

                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                LocSaveButton(
                                    text = stringResource(R.string.btn_save),
                                    onClick = {
                                        if (nameInput.isBlank()) {
                                            nameError = true
                                            return@LocSaveButton
                                        }
                                        locationViewModel.updateLocation(
                                            location.copy(
                                                name = nameInput.trim(),
                                                description = descInput.trim(),
                                                backgroundId = selectedBackgroundId
                                            )
                                        )
                                        isEditing = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBackgroundPicker) {
            BackgroundPickerSheet(
                currentBackgroundId = selectedBackgroundId,
                backgroundType = DreamBackgrounds.Type.LOCATION,
                onBackgroundSelected = {
                    selectedBackgroundId = it
                    showBackgroundPicker = false
                },
                onDismiss = { showBackgroundPicker = false }
            )
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        CircularProgressIndicator(color = GoldLightV, strokeWidth = 2.dp)
    }
}

@Composable
fun LocStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF3B1A58).copy(0.72f),
                        Color(0xFF1A0C30).copy(0.82f)
                    )
                )
            )
            .border(1.dp, GoldLightV.copy(0.20f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = GoldLightV, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(0.52f), fontSize = 10.sp)
        }
    }
}

@Composable
fun StatChip(modifier: Modifier = Modifier, label: String, value: String) =
    LocStatCard(modifier, label, value)

private fun dreamWord(count: Int): String {
    val l2 = count % 100
    val l1 = count % 10
    return when {
        l2 in 11..19 -> "снов"
        l1 == 1 -> "сон"
        l1 in 2..4 -> "сна"
        else -> "снов"
    }
}
