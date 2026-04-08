package com.dreamjournal.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.CharacterWithDreams
import com.dreamjournal.journalofdream.model.DreamCharacter
import com.dreamjournal.journalofdream.sync.CharacterRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val remoteDb = FirebaseFirestore.getInstance()
    private val repository = CharacterRepository(db, auth, remoteDb)

    private val _characters = MediatorLiveData<List<DreamCharacter>>()
    val characters: LiveData<List<DreamCharacter>> get() = _characters

    private val _currentOwnerUid = MutableLiveData<String>()
    private var charactersSource: LiveData<List<DreamCharacter>>? = null

    private val _syncError = MutableLiveData<String?>(null)
    val syncError: LiveData<String?> get() = _syncError

    init {
        val user = auth.currentUser
        _currentOwnerUid.value = user?.uid ?: "guest"
        _characters.addSource(_currentOwnerUid) { newUid ->
            charactersSource?.let { _characters.removeSource(it) }
            val newSource = db.characterDao().getCharactersByOwner(newUid)
            charactersSource = newSource
            _characters.addSource(newSource) { list -> _characters.value = list }
        }
        if (user != null) {
            repository.onSyncError = { _syncError.postValue(it) }
            repository.startSync()
        }
    }

    fun onUserLogin(user: FirebaseUser) {
        _currentOwnerUid.value = user.uid
        viewModelScope.launch {
            repository.migrateGuestCharactersToUser(user.uid)
            repository.startSync()
        }
    }

    fun onUserLogout() {
        repository.stopSync()
        _currentOwnerUid.value = "guest"
    }

    /**
     * Добавить образ с опциональным фоном.
     */
    fun addCharacter(name: String, description: String, backgroundId: Int = 0) {
        val uid = _currentOwnerUid.value ?: "guest"
        viewModelScope.launch {
            val result = repository.upsertCharacter(
                DreamCharacter(ownerUid = uid, name = name, description = description, backgroundId = backgroundId)
            )
            if (result.isFailure) _syncError.postValue("Персонаж сохранён локально, но не синхронизирован")
        }
    }

    fun updateCharacter(character: DreamCharacter) {
        val uid = _currentOwnerUid.value ?: "guest"
        viewModelScope.launch {
            val result = repository.upsertCharacter(character.copy(ownerUid = uid))
            if (result.isFailure) _syncError.postValue("Изменения персонажа сохранены локально, но не синхронизированы")
        }
    }

    fun deleteCharacter(character: DreamCharacter) {
        viewModelScope.launch {
            val result = repository.deleteCharacter(character)
            if (result.isFailure) _syncError.postValue("Персонаж удалён локально, но не синхронизирован")
        }
    }

    fun clearSyncError() { _syncError.value = null }

    fun getCharacterById(characterId: Int): LiveData<DreamCharacter> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.characterDao().getCharacterById(characterId, uid)
    }

    fun getCharacterWithDreams(characterId: Int): LiveData<CharacterWithDreams> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.characterDao().getCharacterWithDreams(characterId, uid)
    }

    fun getAllCharactersWithDreams(): LiveData<List<CharacterWithDreams>> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.characterDao().getAllCharactersWithDreams(uid)
    }
}
