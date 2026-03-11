package com.dreamjournal.journalofdream.ui.dreams

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel

// Доступные цвета для категорий
val categoryColors = listOf(
    "#9C27B0", // фиолетовый
    "#3F51B5", // синий
    "#2196F3", // голубой
    "#009688", // бирюзовый
    "#4CAF50", // зелёный
    "#FF9800", // оранжевый
    "#F44336", // красный
    "#E91E63", // розовый
    "#795548", // коричневый
    "#607D8B", // серо-синий
)

fun hexToColor(hex: String): Color {
    return try {
        val clean = hex.trimStart('#')
        val colorInt = clean.toLong(16).toInt()
        Color(
            red = ((colorInt shr 16) and 0xFF) / 255f,
            green = ((colorInt shr 8) and 0xFF) / 255f,
            blue = (colorInt and 0xFF) / 255f
        )
    } catch (e: Exception) {
        Color(0xFF9C27B0)
    }
}

@Composable
fun CategoryManagerDialog(
    categoryViewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onCategorySelected: (Category?) -> Unit
) {
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(categoryColors.first()) }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cat_title)) },
        text = {
            Column {
                // Поле добавления новой категории
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.cat_new)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Выбор цвета
                Text(
                    text = stringResource(R.string.cat_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryColors) { hex ->
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(hexToColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            val newCat = Category(
                                name = newCategoryName.trim(),
                                isCustom = true,
                                color = selectedColor
                            )
                            categoryViewModel.addCategory(newCat)
                            newCategoryName = ""
                            selectedColor = categoryColors.first()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.btn_add))
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.cat_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(categories) { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            // Цветная точка
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(hexToColor(cat.color))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    onCategorySelected(cat)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = cat.name,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (cat.isCustom) {
                                IconButton(
                                    onClick = { showDeleteConfirm = cat },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.btn_delete),
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
        }
    )

    showDeleteConfirm?.let { cat ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.cat_delete_title)) },
            text = { Text("\"${cat.name}\" будет удалена. Сны с этой категорией останутся, но категория у них сбросится.") },
            confirmButton = {
                TextButton(onClick = {
                    categoryViewModel.deleteCategory(cat)
                    showDeleteConfirm = null
                }) {
                    Text(stringResource(R.string.btn_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }
}
