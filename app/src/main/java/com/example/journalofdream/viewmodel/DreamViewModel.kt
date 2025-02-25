package com.example.journalofdream.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.*
import com.example.journalofdream.sync.DreamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {

    private val localDb = AppDatabase.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val remoteDb = FirebaseFirestore.getInstance()
    private val repository = DreamRepository(localDb, auth, remoteDb)

    // Локации
    val allLocations: LiveData<List<Location>> = localDb.locationDao().getAllLocations()

    // Храним "guest" или userUid
    private val _currentOwnerUid = MutableLiveData<String>()

    // MediatorLiveData, которая будет «переключаться» при изменении _currentOwnerUid
    private val _dreams = MediatorLiveData<List<Dream>>()
    val dreams: LiveData<List<Dream>> = _dreams

    init {
        // Если user!=null, ownerUid=user.uid, иначе "guest"
        val user = auth.currentUser
        _currentOwnerUid.value = user?.uid ?: "guest"

        // Подписываемся на _currentOwnerUid
        _dreams.addSource(_currentOwnerUid) { newUid ->
            // При каждом изменении newUid, нам нужно «переподключиться» к DAO
            // Удаляем старые источники (Room LiveData) и добавляем новый
            updateDreamsSource(newUid)
        }
    }

    private var dreamsSource: LiveData<List<Dream>>? = null

    /**
     * Переподключаемся к DAO getDreamsByOwner(newUid),
     * удаляем предыдущий Source (если был),
     * добавляем новый.
     */
    private fun updateDreamsSource(newUid: String) {
        // 1) Если уже есть активный Source, убираем
        dreamsSource?.let {
            _dreams.removeSource(it)
        }
        // 2) Запрашиваем новое LiveData от DAO
        val newSource = localDb.dreamDao().getDreamsByOwner(newUid)
        dreamsSource = newSource
        // 3) Добавляем как источник MediatorLiveData
        _dreams.addSource(newSource) { list ->
            _dreams.value = list
        }
    }

    // -------------------- Методы --------------------------------

    fun addDream(dream: Dream, locationIds: List<Int>) {
        val uid = _currentOwnerUid.value ?: "guest"
        val finalDream = dream.copy(ownerUid = uid)

        viewModelScope.launch {
            localDb.dreamDao().insert(finalDream)
            // связи
            locationIds.forEach { locId ->
                localDb.dreamDao().insertDreamLocationCrossRef(
                    DreamLocationCrossRef(finalDream.id, locId)
                )
            }
            // Firestore
            repository.upsertDream(finalDream)
        }
    }

    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        val uid = _currentOwnerUid.value ?: "guest"
        val finalDream = updatedDream.copy(ownerUid = uid)
        viewModelScope.launch {
            localDb.dreamDao().update(finalDream)
            localDb.dreamDao().deleteDreamLocationCrossRefs(finalDream.id)
            locationIds.forEach { locId ->
                val crossRef = DreamLocationCrossRef(finalDream.id, locId)
                localDb.dreamDao().insertDreamLocationCrossRef(crossRef)
            }
            repository.upsertDream(finalDream)
        }
    }

    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            localDb.dreamDao().deleteDreamLocationCrossRefs(dream.id)
            repository.deleteDream(dream)
        }
    }

    fun getDreamWithLocationsById(dreamId: String): LiveData<DreamWithLocations> {
        return localDb.dreamDao().getDreamWithLocationsById(dreamId)
    }

    fun searchDreams(searchText: String): LiveData<List<Dream>> {
        val query = "%$searchText%"
        return localDb.dreamDao().searchDreams(query)
    }

    // Синхронизация
    fun onUserLogin() {
        val user = auth.currentUser ?: return
        val userUid = user.uid
        viewModelScope.launch {
            repository.migrateGuestRecordsToUser(userUid)
        }
        repository.startSync()

        // Меняем _currentOwnerUid -> userUid
        _currentOwnerUid.value = userUid
    }

    fun onUserLogout() {
        val user = auth.currentUser
        if (user != null) {
            repository.stopSync()
            auth.signOut()
        }
        _currentOwnerUid.value = "guest"
    }

    fun startSyncIfLoggedIn() {
        if (auth.currentUser != null) {
            repository.startSync()
        }
    }
}
