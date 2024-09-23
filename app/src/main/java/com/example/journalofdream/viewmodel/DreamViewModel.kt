package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.*
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)    // Инициализируем базу данных
    // Initialize the database

    // LiveData list of all dreams
    val allDreams: LiveData<List<Dream>> = db.dreamDao().getAllDreams()

    // LiveData list of all dreams with their locations
    val allDreamsWithLocations: LiveData<List<DreamWithLocations>> = db.dreamDao().getAllDreamsWithLocations()

    // LiveData list of all locations
    val allLocations: LiveData<List<Location>> = db.locationDao().getAllLocations()

    // LiveData list of categories
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        // Initialize categories
        _categories.value = listOf(
            Category(2, "Кошмары", false),
            Category(3, "Осознанные сны", false),
            Category(4, "Сюжетные сны", false),
            Category(5, "Личные сны", false),
            Category(6, "Без категории", false)
        )
    }

    // Method to add a category
    fun addCategory(category: Category) {
        val currentCategories = _categories.value ?: emptyList()
        _categories.value = currentCategories + category
    }

    // Method to delete a category
    fun deleteCategory(categoryId: Int) {
        _categories.value = _categories.value?.filter { it.id != categoryId }
    }

    // Method to add a new dream with locations
    fun addDream(dream: Dream, locationIds: List<Int>) {
        viewModelScope.launch {
            // Insert the dream and get its ID
            val dreamId = db.dreamDao().insert(dream).toInt()
            // Add cross-references for the dream and selected locations
            locationIds.forEach { locationId ->
                val crossRef = DreamLocationCrossRef(dreamId, locationId)
                db.dreamDao().insertDreamLocationCrossRef(crossRef)
            }
        }
    }

    fun getDreamWithLocationsById(dreamId: Int): LiveData<DreamWithLocations> {
        return db.dreamDao().getDreamWithLocationsById(dreamId)
    }


    // Method to delete a dream and its location associations
    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            // Delete cross-references
            db.dreamDao().deleteDreamLocationCrossRefs(dream.id)
            // Delete the dream
            db.dreamDao().delete(dream)
        }
    }

    // Method to update a dream and its locations
    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        viewModelScope.launch {
            // Обновляем данные сна
            db.dreamDao().update(updatedDream)
            // Удаляем старые связи с локациями
            db.dreamDao().deleteDreamLocationCrossRefs(updatedDream.id)
            // Добавляем новые связи с локациями
            locationIds.forEach { locationId ->
                val crossRef = DreamLocationCrossRef(updatedDream.id, locationId)
                db.dreamDao().insertDreamLocationCrossRef(crossRef)
            }
        }
    }

}
