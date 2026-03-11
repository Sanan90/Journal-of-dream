// Файл: com/dreamjournal/journalofdream/ui/common/LogoutButton.kt

package com.dreamjournal.journalofdream.ui.common

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    Button(onClick = {
        auth.signOut()
        onLogout()
    }) {
        Text("Выйти")
    }
}
