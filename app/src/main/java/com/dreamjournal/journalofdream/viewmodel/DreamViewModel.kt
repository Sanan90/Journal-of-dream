package com.dreamjournal.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.DreamWithLocations
import com.dreamjournal.journalofdream.sync.DreamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {

    private val localDb = AppDatabase.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val remoteDb = FirebaseFirestore.getInstance()
    private val repository = DreamRepository(localDb, auth, remoteDb)

    // LiveData списка снов текущего пользователя (mediator, переключается при смене ownerUid)
    private val _dreams = MediatorLiveData<List<Dream>>()
    val dreams: LiveData<List<Dream>> get() = _dreams

    private val _currentOwnerUid = MutableLiveData<String>()
    private var dreamsSource: LiveData<List<Dream>>? = null

    private val _syncError = MutableLiveData<String?>(null)
    val syncError: LiveData<String?> get() = _syncError

    init {
        // Устанавливаем начального владельца снов: если пользователь залогинен – его UID, иначе "guest"
        val user = auth.currentUser
        _currentOwnerUid.value = user?.uid ?: "guest"

        // При изменении текущего владельца (guest->user или наоборот) переключаем источник данных _dreams
        _dreams.addSource(_currentOwnerUid) { newUid ->
            // убираем старый источник
            dreamsSource?.let { _dreams.removeSource(it) }
            // подписываемся на новый источник из DAO
            val newSource = localDb.dreamDao().getDreamsByOwner(newUid)
            dreamsSource = newSource
            _dreams.addSource(newSource) { list -> _dreams.value = list }
        }

        // Если при запуске уже есть авторизованный пользователь, можно запустить синхронизацию:
        if (user != null) {
            repository.onSyncError = { message -> _syncError.postValue(message) }
            repository.startSync()
        }
    }

    /**
     * Добавить новый сон вместе с выбранными локациями.
     */
    fun addDream(dream: Dream, locationIds: List<Int>) {
        val uid = auth.currentUser?.uid ?: "guest"
        val finalDream = dream.copy(ownerUid = uid)
        viewModelScope.launch {
            val result = repository.upsertDream(finalDream, locationIds)
            if (result.isFailure) {
                _syncError.postValue("Сон сохранён локально, но не синхронизирован — нет подключения к сети")
            } else {
                // Отмечаем что сон записан сегодня — уведомление не будет показано
                markDreamRecordedToday()
            }
        }
    }

    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        val uid = auth.currentUser?.uid ?: "guest"
        val finalDream = updatedDream.copy(ownerUid = uid)
        viewModelScope.launch {
            val result = repository.upsertDream(finalDream, locationIds)
            if (result.isFailure) {
                _syncError.postValue("Изменения сохранены локально, но не синхронизированы — нет подключения к сети")
            }
        }
    }

    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            localDb.dreamDao().deleteDreamLocationCrossRefs(dream.localId)
            val result = repository.deleteDream(dream)
            if (result.isFailure) {
                _syncError.postValue("Сон удалён локально, но не синхронизирован — нет подключения к сети")
            }
        }
    }

    fun clearSyncError() {
        _syncError.value = null
    }

    private fun markDreamRecordedToday() {
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = now.get(java.util.Calendar.MONTH) + 1
        val day = now.get(java.util.Calendar.DAY_OF_MONTH)
        val key = "dream_recorded_${year}_${month}_${day}"
        val prefs = getApplication<android.app.Application>()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, true).apply()
    }

    /**
     * Получить LiveData сна по его ID вместе с привязанными локациями.
     * Используется для отображения деталей сна и предварительного выбора локаций при редактировании.
     */
    fun getDreamWithLocationsById(dreamId: Int): LiveData<DreamWithLocations> {
        return localDb.dreamDao().getDreamWithLocationsById(dreamId)
    }

    /**
     * Поиск снов по строке (фильтрует по названию и содержанию среди снов текущего пользователя).
     */
    fun searchDreams(searchText: String): LiveData<List<Dream>> {
        val query = "%$searchText%"
        val ownerUid = _currentOwnerUid.value ?: "guest"
        return localDb.dreamDao().searchDreams(query, ownerUid)
    }

    /**
     * Вызывается при успешном входе пользователя.
     * Мигрирует гостевые записи снов в профиль и начинает синхронизацию.
     */
    fun onUserLogin() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            repository.migrateGuestRecordsToUser(uid)
        }
        // Запускаем синхронизацию с Firestore для снов нового пользователя
        repository.startSync()
        // Переключаем локальный список снов на записи данного пользователя
        _currentOwnerUid.value = uid
    }

    /**
     * Вызывается при выходе из аккаунта.
     * Останавливает синхронизацию с Firestore и переключает представление на гостевые данные.
     */
    fun onUserLogout() {
        if (auth.currentUser != null) {
            repository.stopSync()
        }
        // Переключаем LiveData на гостевой режим (сразу покажет гостевые сны, если они есть)
        // auth.signOut() вызывается в JournalOfDreamApp, здесь не нужен — иначе двойной выход
        _currentOwnerUid.value = "guest"
    }

    /**
     * (Дополнительно) Запустить синхронизацию, если пользователь уже авторизован.
     */
    fun startSyncIfLoggedIn() {
        if (auth.currentUser != null) {
            repository.startSync()
        }
    }
}
