package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.CharacterWithDreams
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel

enum class CharacterSort {
    BY_DREAMS,
    BY_NAME,
    BY_LAST_DREAM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    navController: NavHostController,
    characterViewModel: CharacterViewModel
) {
    val charactersWithDreams by characterViewModel.getAllCharactersWithDreams().observeAsState(emptyList())
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(CharacterSort.BY_DREAMS) }
    var pendingDelete by remember { mutableStateOf<CharacterWithDreams?>(null) }

    val sortedCharacters = remember(charactersWithDreams, currentSort) {
        when (currentSort) {
            CharacterSort.BY_DREAMS -> charactersWithDreams.sortedByDescending { it.dreams.size }
            CharacterSort.BY_NAME -> charactersWithDreams.sortedBy { it.character.name.lowercase() }
            CharacterSort.BY_LAST_DREAM -> charactersWithDreams.sortedByDescending { cwd ->
                cwd.dreams.mapNotNull { it.date.ifBlank { null } }.maxOrNull() ?: ""
            }
        }
    }

    val sortLabel = when (currentSort) {
        CharacterSort.BY_DREAMS -> stringResource(R.string.locations_sort_popular)
        CharacterSort.BY_NAME -> stringResource(R.string.locations_sort_alpha)
        CharacterSort.BY_LAST_DREAM -> stringResource(R.string.locations_sort_last)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.characters_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("addCharacter") }) {
                        Icon(Icons.Default.Add, stringResource(R.string.characters_add), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            BackgroundScreen()

            if (charactersWithDreams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.characters_empty),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.characters_empty_hint),
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.characters_total, charactersWithDreams.size),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Box {
                                AssistChip(
                                    onClick = { showSortMenu = true },
                                    label = { Text(sortLabel, fontSize = 12.sp) }
                                )
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_popular)) },
                                        onClick = {
                                            currentSort = CharacterSort.BY_DREAMS
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == CharacterSort.BY_DREAMS) Text("✓")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_alpha)) },
                                        onClick = {
                                            currentSort = CharacterSort.BY_NAME
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == CharacterSort.BY_NAME) Text("✓")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.locations_sort_last)) },
                                        onClick = {
                                            currentSort = CharacterSort.BY_LAST_DREAM
                                            showSortMenu = false
                                        },
                                        trailingIcon = {
                                            if (currentSort == CharacterSort.BY_LAST_DREAM) Text("✓")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    items(sortedCharacters, key = { it.character.id }) { cwd ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    pendingDelete = cwd
                                }
                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                val scale by animateFloatAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                                    label = "characterDeleteScale"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                        .background(
                                            color = Color.Red.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(end = 24.dp)
                                            .scale(scale)
                                            .size(28.dp)
                                    )
                                }
                            }
                        ) {
                            CharacterCard(
                                item = cwd,
                                onClick = { navController.navigate("viewCharacter/${cwd.character.id}") }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { cwd ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_character_title)) },
            text = { Text(stringResource(R.string.dialog_delete_character_message, cwd.character.name)) },
            confirmButton = {
                TextButton(onClick = {
                    characterViewModel.deleteCharacter(cwd.character)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.btn_delete), color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun CharacterCard(
    item: CharacterWithDreams,
    onClick: () -> Unit
) {
    val dreamCount = item.dreams.size
    val character = item.character
    val lastDreamDate = item.dreams.mapNotNull { it.date.ifBlank { null } }.maxOrNull()
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2D1B69),
                            Color(0xFF1A1040)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7E57C2).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name.ifEmpty { "(Без названия)" },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (lastDreamDate != null) {
                            stringResource(R.string.locations_last_dream, lastDreamDate)
                        } else {
                            stringResource(R.string.locations_no_dreams)
                        },
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (dreamCount > 0) Color(0xFF7E57C2).copy(alpha = 0.35f)
                    else Color.White.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$dreamCount",
                            color = if (dreamCount > 0) Color.White else Color.White.copy(alpha = 0.4f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dreamWord(dreamCount, pluralOne, pluralFew, pluralMany),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
