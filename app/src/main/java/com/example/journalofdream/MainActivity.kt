package com.example.journalofdream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.journalofdream.ui.JournalOfDreamApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JournalOfDreamApp()
        }
    }
}


//// Превью экрана для быстрой проверки интерфейса в Android Studio
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    val navController = rememberNavController()
//    MainScreen(navController)
//}
