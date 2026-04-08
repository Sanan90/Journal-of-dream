package com.dreamjournal.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.dreamjournal.journalofdream.database.AppDatabase
import com.dreamjournal.journalofdream.model.Location
import com.dreamjournal.journalofdream.model.LocationWithDreams
import com.dreamjournal.journalofdream.sync.LocationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = FirebaseAuth.getInstance()
    private val remoteDb = FirebaseFirestore.getInstance()
    private val locationRepository = LocationRepository(db, auth, remoteDb)

    private val _locations = MediatorLiveData<List<Location>>()
    val locations: LiveData<List<Location>> get() = _locations

    private val _currentOwnerUid = MutableLiveData<String>()
    private var locationsSource: LiveData<List<Location>>? = null

    private val _syncError = MutableLiveData<String?>(null)
    val syncError: LiveData<String?> get() = _syncError

    init {
        val user = auth.currentUser
        _currentOwnerUid.value = user?.uid ?: "guest"

        _locations.addSource(_currentOwnerUid) { newUid ->
            locationsSource?.let { _locations.removeSource(it) }
            val newSource = db.locationDao().getLocationsByOwner(newUid)
            locationsSource = newSource
            _locations.addSource(newSource) { list -> _locations.value = list }
        }

        if (user != null) {
            locationRepository.onSyncError = { message -> _syncError.postValue(message) }
            locationRepository.startSync()
        }
    }

    fun onUserLogin(user: FirebaseUser) {
        _currentOwnerUid.value = user.uid
        viewModelScope.launch {
            locationRepository.migrateGuestLocationsToUser(user.uid)
            locationRepository.startSync()
        }
    }

    fun onUserLogout() {
        locationRepository.stopSync()
        _currentOwnerUid.value = "guest"
    }

    /**
     * Добавить локацию с опциональным фоном.
     */
    fun addLocation(name: String, description: String, backgroundId: Int = 0) {
        val uid = _currentOwnerUid.value ?: "guest"
        val newLocation = Location(ownerUid = uid, name = name, description = description, backgroundId = backgroundId)
        viewModelScope.launch {
            val result = locationRepository.upsertLocation(newLocation)
            if (result.isFailure) {
                _syncError.postValue("Локация сохранена локально, но не синхронизирована — нет подключения к сети")
            }
        }
    }

    fun updateLocation(location: Location) {
        val uid = _currentOwnerUid.value ?: "guest"
        val updated = location.copy(ownerUid = uid)
        viewModelScope.launch {
            val result = locationRepository.upsertLocation(updated)
            if (result.isFailure) {
                _syncError.postValue("Изменения сохранены локально, но не синхронизированы — нет подключения к сети")
            }
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            val result = locationRepository.deleteLocation(location)
            if (result.isFailure) {
                _syncError.postValue("Локация удалена локально, но не синхронизирована — нет подключения к сети")
            }
        }
    }

    fun clearSyncError() { _syncError.value = null }

    fun getLocationById(locationId: Int): LiveData<Location> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.locationDao().getLocationById(locationId, uid)
    }

    fun getLocationWithDreams(locationId: Int): LiveData<LocationWithDreams> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.locationDao().getLocationWithDreams(locationId, uid)
    }

    fun getAllLocationsWithDreams(): LiveData<List<LocationWithDreams>> {
        val uid = _currentOwnerUid.value ?: "guest"
        return db.locationDao().getAllLocationsWithDreams(uid)
    }
}
