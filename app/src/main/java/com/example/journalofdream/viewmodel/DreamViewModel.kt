// Файл: com/example/journalofdream/viewmodel/DreamViewModel.kt

package com.example.journalofdream.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val localDb = AppDatabase.getInstance(application)
    private val remoteDb = FirebaseFirestore.getInstance()

    val allDreams: LiveData<List<Dream>> = if (auth.currentUser != null) {
        MutableLiveData<List<Dream>>().also { liveData ->
            val userId = auth.currentUser!!.uid
            remoteDb.collection("users").document(userId).collection("dreams")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        // Обработка ошибки
                        Log.e("DreamViewModel", "Error fetching dreams", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val dreamList = snapshot.documents.mapNotNull { doc ->
                            val dream = doc.toObject(Dream::class.java)
                            dream?.copy(id = doc.id)
                        }
                        liveData.value = dreamList
                    }
                }
        }
    } else {
        localDb.dreamDao().getAllDreams()
    }

    val allLocations: LiveData<List<Location>> = localDb.locationDao().getAllLocations()

    // LiveData список категорий
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        // Инициализируем категории
        _categories.value = listOf(
            Category(2, "Кошмары", false),
            Category(3, "Осознанные сны", false),
            Category(4, "Сюжетные сны", false),
            Category(5, "Личные сны", false),
            Category(6, "Без категории", false)
        )
    }

    // Метод для добавления категории
    fun addCategory(category: Category) {
        val currentCategories = _categories.value ?: emptyList()
        _categories.value = currentCategories + category
    }

    // Метод для удаления категории
    fun deleteCategory(categoryId: Int) {
        _categories.value = _categories.value?.filter { it.id != categoryId }
    }

    // Метод для добавления нового сна с локациями
    fun addDream(dream: Dream, locationIds: List<Int>) {
        if (auth.currentUser != null) {
            // Пользователь авторизован, сохраняем в Firestore
            val userId = auth.currentUser!!.uid
            remoteDb.collection("users").document(userId).collection("dreams")
                .document(dream.id)
                .set(dream)
                .addOnSuccessListener {
                    // Успешно сохранено
                }
                .addOnFailureListener { e ->
                    // Обработка ошибки
                    Log.e("DreamViewModel", "Error adding dream", e)
                }
        } else {
            // Пользователь не авторизован, сохраняем в локальную базу данных
            viewModelScope.launch {
                // Вставляем сон
                localDb.dreamDao().insert(dream)
                // Добавляем связи с локациями
                locationIds.forEach { locationId ->
                    val crossRef = DreamLocationCrossRef(dream.id, locationId)
                    localDb.dreamDao().insertDreamLocationCrossRef(crossRef)
                }
            }
        }
    }


    // Метод для удаления сна и его связей с локациями
    fun deleteDream(dream: Dream) {
        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            remoteDb.collection("users").document(userId).collection("dreams").document(dream.id)
                .delete()
                .addOnSuccessListener {
                    // Успешно удалено
                }
                .addOnFailureListener { e ->
                    // Обработка ошибки
                    Log.e("DreamViewModel", "Error deleting dream", e)
                }
        } else {
            viewModelScope.launch {
                localDb.dreamDao().deleteDreamLocationCrossRefs(dream.id)
                localDb.dreamDao().delete(dream)
            }
        }
    }

    // Метод для обновления сна и его локаций
    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        if (auth.currentUser != null) {
            val userId = auth.currentUser!!.uid
            remoteDb.collection("users").document(userId).collection("dreams").document(updatedDream.id)
                .set(updatedDream)
                .addOnSuccessListener {
                    // Успешно обновлено
                }
                .addOnFailureListener { e ->
                    // Обработка ошибки
                    Log.e("DreamViewModel", "Error updating dream", e)
                }
        } else {
            viewModelScope.launch {
                localDb.dreamDao().update(updatedDream)
                // Обновляем связи с локациями
                localDb.dreamDao().deleteDreamLocationCrossRefs(updatedDream.id)
                locationIds.forEach { locationId ->
                    val crossRef = DreamLocationCrossRef(updatedDream.id, locationId)
                    localDb.dreamDao().insertDreamLocationCrossRef(crossRef)
                }
            }
        }
    }

    // Метод для получения сна с локациями по ID
    fun getDreamWithLocationsById(dreamId: String): LiveData<DreamWithLocations> {
        return localDb.dreamDao().getDreamWithLocationsById(dreamId)
    }
}
