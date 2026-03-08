package com.example.journalofdream.ui.locations

import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel
) {
    val locationState = locationViewModel.getLocationById(locationId).observeAsState()
    var locationName by remember { mutableStateOf("") }
    var locationDescription by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(locationState.value) {
        locationState.value?.let { loc ->
            locationName = loc.name
            locationDescription = loc.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.location_edit_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
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

            val location = locationState.value
            if (location == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text(stringResource(R.string.location_field_name), color = Color.White.copy(alpha = 0.7f)) },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = locationDescription,
                        onValueChange = { locationDescription = it },
                        label = { Text(stringResource(R.string.location_field_desc), color = Color.White.copy(alpha = 0.7f)) },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val updated = location.copy(
                                name = locationName.trim(),
                                description = locationDescription.trim()
                            )
                            locationViewModel.updateLocation(updated)
                            keyboardController?.hide()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(stringResource(R.string.dream_save_changes), color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            // Диалог подтверждения удаления
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.dialog_delete_location_title)) },
                    text = { Text(stringResource(R.string.dialog_delete_location_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            locationState.value?.let { locationViewModel.deleteLocation(it) }
                            navController.popBackStack()
                        }) {
                            Text(stringResource(R.string.btn_delete), color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                )
            }
        }
    }
}
