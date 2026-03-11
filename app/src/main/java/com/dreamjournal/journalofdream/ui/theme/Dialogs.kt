package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dreamjournal.journalofdream.R

@Composable
fun ChooseActionDialog(
    onDismiss: () -> Unit,
    onDreamSelected: () -> Unit,
    onLocationSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.fab_choose_action)) },
        text = {
            Column {
                Button(
                    onClick = onDreamSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.fab_record_dream))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLocationSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.fab_add_location))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
