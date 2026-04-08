package com.dreamjournal.journalofdream.ui.locations

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.material.icons.filled.Palette
import com.dreamjournal.journalofdream.ui.common.BackgroundPickerSheet
import com.dreamjournal.journalofdream.ui.common.DreamBackgrounds
import com.dreamjournal.journalofdream.ui.common.DreamBackgroundLayer
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

private val GoldLightE = Color(0xFFF0D68C)
private val PlayfairFamilyE = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel
) {
    val locationState by locationViewModel.getLocationById(locationId).observeAsState()
    var locationName        by remember { mutableStateOf("") }
    var locationDescription by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedBackgroundId by remember { mutableStateOf(0) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(locationState) {
        locationState?.let { loc ->
            locationName = loc.name
            locationDescription = loc.description
            selectedBackgroundId = loc.backgroundId
        }
    }

    if (showDeleteDialog) {
        LocAlertDialog(
            title = stringResource(R.string.dialog_delete_location_title),
            message = stringResource(R.string.dialog_delete_location_message),
            confirmText = stringResource(R.string.btn_delete),
            confirmColor = Color(0xFFEF5350),
            onConfirm = {
                locationState?.let { locationViewModel.deleteLocation(it) }
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        DreamBackgroundLayer(selectedBackgroundId)

        if (locationState == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldLightE, strokeWidth = 2.dp)
            }
        } else {
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                // Заголовок
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = GoldLightE, modifier = Modifier.size(28.dp))
                    }
                    Text(stringResource(R.string.location_edit_title), color = GoldLightE,
                        fontFamily = PlayfairFamilyE, fontWeight = FontWeight.Bold,
                        fontSize = 26.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showBackgroundPicker = true }) {
                        Icon(Icons.Default.Palette, null,
                            tint = if (selectedBackgroundId != 0) GoldLightE else GoldLightE.copy(0.4f),
                            modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(22.dp))
                    }
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
                        onValueChange = { locationName = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = locFieldColors()
                    )

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
                        text = stringResource(R.string.dream_save_changes),
                        modifier = Modifier.align(Alignment.End),
                        onClick = {
                            locationState?.let { loc ->
                                locationViewModel.updateLocation(loc.copy(
                                    name = locationName.trim(),
                                    description = locationDescription.trim(),
                                    backgroundId = selectedBackgroundId
                                ))
                            }
                            keyboardController?.hide()
                            navController.popBackStack()
                        }
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}
