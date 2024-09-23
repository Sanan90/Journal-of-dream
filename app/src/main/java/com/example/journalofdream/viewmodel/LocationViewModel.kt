package com.example.journalofdream.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.journalofdream.database.AppDatabase
import com.example.journalofdream.model.*
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    // LiveData list of all locations
    val allLocations: LiveData<List<Location>> = db.locationDao().getAllLocations()

    // Method to add a new location
    fun addLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().insert(location)
        }
    }

    // Method to get a location by its ID
    fun getLocationById(locationId: Int): LiveData<Location> {
        return db.locationDao().getLocationById(locationId)
    }

    // Method to delete a location
    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().delete(location)
        }
    }

    // Method to update a location
    fun updateLocation(location: Location) {
        viewModelScope.launch {
            db.locationDao().update(location)
        }
    }

    // Method to get dreams associated with a location
    fun getDreamsByLocation(locationId: Int): LiveData<List<Dream>> {
        return db.dreamDao().getDreamsByLocation(locationId)
    }

    // Method to get a location with its associated dreams
    fun getLocationWithDreams(locationId: Int): LiveData<LocationWithDreams> {
        return db.locationDao().getLocationWithDreams(locationId)
    }
}
