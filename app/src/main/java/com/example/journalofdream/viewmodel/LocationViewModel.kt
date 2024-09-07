package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.util.DatabaseBuilder
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseBuilder.getInstance(application)

    // Получение всех локаций
    val allLocations: LiveData<List<Location>> = db.locationDao().getAllLocations()

    // Метод для добавления новой локации
    fun addLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().insert(location)
        }
    }

    // Метод для получения локации по её ID
    fun getLocationById(locationId: Int): LiveData<Location> {
        return db.locationDao().getLocationById(locationId)
    }

    // Метод для удаления локации
    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().delete(location)
        }
    }

    // Метод для обновления локации
    fun updateLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().update(location)
        }
    }

    // Метод для получения списка снов, связанных с локацией
    fun getDreamsByLocation(locationId: Int): LiveData<List<Dream>> {
        return db.dreamDao().getDreamsByLocation(locationId)
    }
}
