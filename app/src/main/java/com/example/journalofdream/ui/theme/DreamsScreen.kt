package com.example.journalofdream.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.ui.common.TopBar
import com.example.journalofdream.model.Dream
import com.example.journalofdream.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@Composable
fun DreamsScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {
    val allDreams by dreamViewModel.allDreams.observeAsState(listOf())

    val sortedDreams = allDreams
        .filter { it.content.isNotBlank() && it.date.isNotBlank() }
        .sortedByDescending { dream ->
            try {
                dateFormat.parse(dream.date)
            } catch (e: Exception) {
                null
            }
        }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                onSaveClick = { navController.navigate("addDream") }
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundScreen()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedDreams) { dream ->
                        DreamListItem(dream, navController)
                    }
                }
            }
        }
    )
}

@Composable
fun DreamListItem(dream: Dream, navController: NavHostController) {
    val previewText = dream.content.take(30) + if (dream.content.length > 30) "..." else ""

    Button(
        onClick = { navController.navigate("editDream/${dream.id}") },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = previewText,
                color = Color.Black,
                fontSize = 18.sp,
                maxLines = 1
            )
            Text(
                text = dream.date,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
