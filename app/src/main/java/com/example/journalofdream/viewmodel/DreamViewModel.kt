package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.DreamWithLocations
import com.example.journalofdream.sync.DreamRepository
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
            repository.startSync()  // запускаем слушатель изменений Firestore для снов
        }
    }

    /**
     * Добавить новый сон вместе с выбранными локациями.
     */
    fun addDream(dream: Dream, locationIds: List<Int>) {
        // Устанавливаем текущего владельца (UID пользователя или "guest") перед сохранением
        val uid = auth.currentUser?.uid ?: "guest"
        val finalDream = dream.copy(ownerUid = uid)
        viewModelScope.launch {
            // ИСПРАВЛЕНО: вместо прямой вставки в DAO используем репозиторий,
            // который сохранит сон и связи в базе, а также синхронизирует с Firestore.
            repository.upsertDream(finalDream, locationIds)
        }
    }

    /**
     * Обновить существующий сон и его связанные локации.
     */
    fun updateDream(updatedDream: Dream, locationIds: List<Int>) {
        val uid = auth.currentUser?.uid ?: "guest"
        val finalDream = updatedDream.copy(ownerUid = uid)
        viewModelScope.launch {
            // ИСПРАВЛЕНО: обновление сна также выполняем через репозиторий (обновит локально и в Firestore).
            repository.upsertDream(finalDream, locationIds)
        }
    }

    /**
     * Удалить сон (и все связанные с ним привязки локаций).
     */
    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            // ИСПРАВЛЕНО: перед удалением сна вручную удаляем все его связи из локальной базы (для надёжности, хотя CASCADE тоже удалит)
            localDb.dreamDao().deleteDreamLocationCrossRefs(dream.localId)
            repository.deleteDream(dream)
        }
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
