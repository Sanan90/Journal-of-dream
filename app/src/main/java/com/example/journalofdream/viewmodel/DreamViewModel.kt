
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

    // Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    // Firestore
    private val remoteDb = FirebaseFirestore.getInstance()

    // Локальная база
    private val localDb = AppDatabase.getInstance(application)


    // 1) Берём все сны из Room (или Firestore, если хотим синхронизировать),
    //    но, как мы договаривались, основа — Room.
    //    Если у вас уже настроена логика "Room + Firestore" => оставьте как есть.
    //    Я просто показываю, что вы можете хранить все сны в Room и дублировать в Firestore при авторизации.
    val allDreams: LiveData<List<Dream>> = localDb.dreamDao().getAllDreams()

    // 2) Локации из локальной базы
    val allLocations: LiveData<List<Location>> = localDb.locationDao().getAllLocations()

    // Добавляем сон + связи
    fun addDream(dream: Dream, locationIds: List<Int>) {
        // 1. Локально в Room
        viewModelScope.launch {
            localDb.dreamDao().insert(dream)
            locationIds.forEach { locationId ->
                localDb.dreamDao().insertDreamLocationCrossRef(
                    DreamLocationCrossRef(dream.id, locationId)
                )
            }
        }

        // 2. При желании синхронизируем в Firestore, если пользователь авторизован
        val userId = auth.currentUser?.uid
        if (userId != null) {
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(dream.id)
                .set(dream)
                .addOnFailureListener { e ->
                    Log.e("DreamViewModel", "Error adding dream to Firestore", e)
                }
        }
    }


    // Метод для удаления сна и его связей с локациями
    // Удаляем сон
    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            // Удаляем связи
            localDb.dreamDao().deleteDreamLocationCrossRefs(dream.id)
            // Удаляем сам сон
            localDb.dreamDao().delete(dream)
        }

        // Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(dream.id)
                .delete()
                .addOnFailureListener { e ->
                    Log.e("DreamViewModel", "Error deleting dream from Firestore", e)
                }
        }
    }

    // Метод для обновления сна и его локаций
    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        // Локально
        viewModelScope.launch {
            localDb.dreamDao().update(updatedDream)
            // Сначала удалим старые связи:
            localDb.dreamDao().deleteDreamLocationCrossRefs(updatedDream.id)
            // Добавим заново:
            locationIds.forEach { locationId ->
                localDb.dreamDao().insertDreamLocationCrossRef(
                    DreamLocationCrossRef(updatedDream.id, locationId)
                )
            }
        }

        // Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            remoteDb.collection("users")
                .document(userId)
                .collection("dreams")
                .document(updatedDream.id)
                .set(updatedDream)
                .addOnFailureListener { e ->
                    Log.e("DreamViewModel", "Error updating dream in Firestore", e)
                }
        }
    }

    // Метод для получения сна с локациями по ID
    fun getDreamWithLocationsById(dreamId: String): LiveData<DreamWithLocations> {
        return localDb.dreamDao().getDreamWithLocationsById(dreamId)
    }


    fun searchDreams(searchText: String): LiveData<List<Dream>> {
        val query = "%$searchText%"
        return localDb.dreamDao().searchDreams(query)
    }
}


//    // 1. Когда пользователь авторизовался — подгрузить данные из Firestore в Room
//    fun syncFromFirestoreToLocal() {
//        val userId = auth.currentUser?.uid ?: return
//        remoteDb.collection("users")
//            .document(userId)
//            .collection("dreams")
//            .get()
//            .addOnSuccessListener { documents ->
//                viewModelScope.launch {
//                    // Пробегаемся по всем документам
//                    for (doc in documents) {
//                        val dream = doc.toObject(Dream::class.java)
//                            ?.copy(id = doc.id) // ID = имя документа
//
//                        if (dream != null) {
//                            localDb.dreamDao().insert(dream)
//                        }
//                    }
//                }
//            }
//    }



