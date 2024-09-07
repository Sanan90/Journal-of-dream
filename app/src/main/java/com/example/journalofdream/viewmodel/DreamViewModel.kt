package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.journalofdream.model.Dream
import com.example.journalofdream.util.DatabaseBuilder
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseBuilder.getInstance(application)

    // Преобразуем список снов в LiveData
    val allDreams: LiveData<List<Dream>> = db.dreamDao().getAllDreams()

    // Метод для сохранения нового сна
    fun add(dream: Dream) {
        viewModelScope.launch {
            db.dreamDao().insert(dream)
        }
    }

    // Метод для удаления сна
    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            db.dreamDao().delete(dream)
        }
    }

    // Метод для обновления сна
    fun updateDream(updatedDream: Dream) {
        viewModelScope.launch {
            db.dreamDao().update(updatedDream)
        }
    }
}