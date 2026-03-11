package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.util.dreamQuotes
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesAdminScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()

    // Цитаты из Firestore
    var firestoreQuotes by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    // Встроенные цитаты — можно "скрыть" (сохраняем индексы скрытых)
    var hiddenBuiltin by remember { mutableStateOf<Set<Int>>(emptySet()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var deletingFirestoreId by remember { mutableStateOf<String?>(null) }
    var hidingBuiltinIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = db.collection("quotes")
            .addSnapshotListener { snap, _ ->
                firestoreQuotes = snap?.documents?.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    doc.id to text
                } ?: emptyList()
                isLoading = false
            }
        onDispose { listener.remove() }
    }

    val visibleBuiltin = dreamQuotes.mapIndexed { i, q -> i to q }.filter { (i, _) -> i !in hiddenBuiltin }
    val totalActive = firestoreQuotes.size + visibleBuiltin.size

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("💬 Цитаты", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, "Добавить", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Инфо-плашка
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ℹ️", fontSize = 20.sp)
                            Column {
                                Text(
                                    "Активных цитат: $totalActive",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Отправляются в 10:00, 16:00 и 22:00",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // --- Раздел: Мои цитаты (Firestore) ---
                item {
                    Text(
                        "✨ Мои цитаты (${firestoreQuotes.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (firestoreQuotes.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
                        ) {
                            Box(
                                Modifier.fillMaxWidth().padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Нет добавленных цитат.\nНажмите + чтобы добавить.",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(firestoreQuotes, key = { _, item -> item.first }) { _, (id, text) ->
                        QuoteCard(
                            text = text,
                            buttonLabel = "Удалить",
                            buttonColor = Color(0xFFEF5350),
                            onAction = { deletingFirestoreId = id }
                        )
                    }
                }

                // --- Раздел: Встроенные цитаты ---
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "📦 Встроенные цитаты (${visibleBuiltin.size}/${dreamQuotes.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "Используются если нет своих. Можно скрыть ненужные.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }

                itemsIndexed(dreamQuotes) { index, text ->
                    val isHidden = index in hiddenBuiltin
                    QuoteCard(
                        text = text,
                        buttonLabel = if (isHidden) "Показать" else "Скрыть",
                        buttonColor = if (isHidden) Color(0xFF66BB6A) else Color(0xFFFF8A65),
                        dimmed = isHidden,
                        onAction = {
                            if (isHidden) {
                                hiddenBuiltin = hiddenBuiltin - index
                            } else {
                                hidingBuiltinIndex = index
                            }
                        }
                    )
                }
            }
        }

        // FAB добавить
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFF7E57C2)
        ) {
            Icon(Icons.Default.Add, "Добавить цитату", tint = Color.White)
        }
    }

    // Диалог добавления
    if (showAddDialog) {
        var newText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("✨ Новая цитата", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        label = { Text(stringResource(R.string.quotes_field_text)) },
                        placeholder = { Text(stringResource(R.string.quotes_field_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) { Text(stringResource(R.string.btn_cancel)) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newText.isNotBlank()) {
                                    db.collection("quotes").add(mapOf("text" to newText.trim()))
                                    showAddDialog = false
                                }
                            },
                            enabled = newText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                        ) { Text("Добавить") }
                    }
                }
            }
        }
    }

    // Подтверждение удаления из Firestore
    deletingFirestoreId?.let { id ->
        val text = firestoreQuotes.find { it.first == id }?.second ?: ""
        AlertDialog(
            onDismissRequest = { deletingFirestoreId = null },
            title = { Text(stringResource(R.string.quotes_delete_title)) },
            text = { Text("\"$text\"", fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("quotes").document(id).delete()
                    deletingFirestoreId = null
                }) { Text(stringResource(R.string.btn_delete), color = Color(0xFFEF5350)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingFirestoreId = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    // Подтверждение скрытия встроенной
    hidingBuiltinIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { hidingBuiltinIndex = null },
            title = { Text(stringResource(R.string.quotes_hide_title)) },
            text = { Text("\"${dreamQuotes[index]}\"", fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    hiddenBuiltin = hiddenBuiltin + index
                    hidingBuiltinIndex = null
                }) { Text(stringResource(R.string.quotes_hide_btn), color = Color(0xFFFF8A65)) }
            },
            dismissButton = {
                TextButton(onClick = { hidingBuiltinIndex = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}

@Composable
private fun QuoteCard(
    text: String,
    buttonLabel: String,
    buttonColor: Color,
    dimmed: Boolean = false,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = if (dimmed) 0.04f else 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\"$text\"",
                color = Color.White.copy(alpha = if (dimmed) 0.35f else 0.9f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(buttonLabel, color = buttonColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
