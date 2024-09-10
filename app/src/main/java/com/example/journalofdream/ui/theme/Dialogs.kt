package com.example.journalofdream.ui.theme

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChooseActionDialog(
    onDismiss: () -> Unit,
    onDreamSelected: () -> Unit,
    onLocationSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Выберите действие") },
        text = {
            Column {
                Button(
                    onClick = onDreamSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Записать сновидение")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLocationSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить локацию")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
