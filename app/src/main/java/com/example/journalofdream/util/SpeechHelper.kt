package com.example.journalofdream.util

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.util.Locale

/**
 * Composable-хелпер для запуска системного распознавания речи.
 * Возвращает функцию-лямбду для вызова диалога диктовки.
 * onResult вызывается с распознанным текстом.
 */
@Composable
fun rememberSpeechLauncher(onResult: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognized = matches?.firstOrNull() ?: return@rememberLauncherForActivityResult
            onResult(recognized)
        }
    }

    return {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Расскажите свой сон...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        launcher.launch(intent)
    }
}
