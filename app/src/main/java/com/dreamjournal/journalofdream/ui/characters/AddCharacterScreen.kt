package com.dreamjournal.journalofdream.ui.characters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCharacterScreen(
    navController: NavHostController,
    characterViewModel: CharacterViewModel
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasUnsavedChanges = name.isNotBlank() || description.isNotBlank()

    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_character_message)) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; navController.popBackStack() }) {
                    Text(stringResource(R.string.discard_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.discard_dismiss)) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.character_add_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { if (hasUnsavedChanges) showExitDialog = true else navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            BackgroundScreen()
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; if (it.isNotBlank()) showError = false },
                    label = { Text(stringResource(R.string.character_field_name), color = Color.White.copy(alpha = 0.7f)) },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    isError = showError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        errorBorderColor = Color.Red
                    )
                )
                if (showError) {
                    Text(stringResource(R.string.character_error_name), color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.character_field_desc), color = Color.White.copy(alpha = 0.7f)) },
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            showError = true
                            return@Button
                        }
                        characterViewModel.addCharacter(name.trim(), description.trim())
                        keyboardController?.hide()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) { Text(stringResource(R.string.btn_save), color = Color.White, fontSize = 16.sp) }
            }
        }
    }
}
