package com.example.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.journalofdream.model.Location
import com.example.journalofdream.model.LocationWithDreams

@Dao
interface LocationDao {

    // Insert a new location and return its ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location): Long

    // Update an existing location
    @Update
    suspend fun update(location: Location)

    // Delete a location
    @Delete
    suspend fun delete(location: Location)

    // Get all locations
    @Query("SELECT * FROM locations")
    fun getAllLocations(): LiveData<List<Location>>

    // Get a location by its ID
    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationById(locationId: Int): LiveData<Location>

    // Get a location with its associated dreams
    @Transaction
    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationWithDreams(locationId: Int): LiveData<LocationWithDreams>
}
