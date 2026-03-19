package com.dreamjournal.journalofdream.ui.characters

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.ui.dreams.DreamListItem
import com.dreamjournal.journalofdream.ui.locations.StatChip
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDateShort(date: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        output.format(input.parse(date)!!)
    } catch (_: Exception) {
        date
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCharacterScreen(
    navController: NavHostController,
    characterId: Int,
    characterViewModel: CharacterViewModel
) {
    val characterWithDreams by characterViewModel.getCharacterWithDreams(characterId).observeAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    characterWithDreams?.let { cwd ->
        val character = cwd.character
        val dreams = cwd.dreams
        val dreamCount = dreams.size
        val firstDream = dreams.minByOrNull { it.date }
        val lastDream = dreams.maxByOrNull { it.date }

        LaunchedEffect(character) {
            if (!isEditing) {
                nameInput = character.name
                descInput = character.description
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) stringResource(R.string.character_edit_title) else character.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEditing) {
                                isEditing = false
                                nameError = false
                                nameInput = character.name
                                descInput = character.description
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = if (isEditing) stringResource(R.string.btn_cancel) else stringResource(R.string.btn_back),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (!isEditing) {
                            IconButton(onClick = {
                                nameInput = character.name
                                descInput = character.description
                                isEditing = true
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = Color.White)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = Color(0xFFEF5350).copy(alpha = 0.9f))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()

                if (!isEditing) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (character.description.isNotBlank()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text(
                                        text = character.description,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatChip(
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(R.string.location_stat_dreams),
                                    value = "$dreamCount"
                                )
                                StatChip(
                                    modifier = Modifier.weight(1.6f),
                                    label = stringResource(R.string.location_stat_first),
                                    value = if (firstDream != null) formatDateShort(firstDream.date) else "—"
                                )
                                StatChip(
                                    modifier = Modifier.weight(1.6f),
                                    label = stringResource(R.string.location_stat_last),
                                    value = if (lastDream != null) formatDateShort(lastDream.date) else "—"
                                )
                            }
                        }

                        item {
                            Text(
                                text = if (dreamCount > 0) stringResource(R.string.character_dreams_title) else stringResource(R.string.characters_empty_hint),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (dreams.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌙", fontSize = 36.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                stringResource(R.string.character_dreams_empty),
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.isNotBlank()) nameError = false
                            },
                            label = { Text(stringResource(R.string.character_field_name)) },
                            textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text(stringResource(R.string.character_field_name)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7E57C2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = Color(0xFF7E57C2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text(stringResource(R.string.character_field_desc_optional)) },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
                            minLines = 5,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7E57C2),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedLabelColor = Color(0xFF7E57C2),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (nameInput.isBlank()) {
                                    nameError = true
                                    return@Button
                                }
                                characterViewModel.updateCharacter(
                                    character.copy(
                                        name = nameInput.trim(),
                                        description = descInput.trim()
                                    )
                                )
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.btn_save))
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.dialog_delete_character_title)) },
                text = { Text(stringResource(R.string.dialog_delete_character_message, character.name)) },
                confirmButton = {
                    TextButton(onClick = {
                        characterViewModel.deleteCharacter(character)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }) {
                        Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BackgroundScreen()
        CircularProgressIndicator(color = Color.White)
    }
}
