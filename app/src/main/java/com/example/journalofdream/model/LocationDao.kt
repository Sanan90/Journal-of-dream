package com.example.journalofdream.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): LiveData<List<Location>>

    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationById(locationId: Int): LiveData<Location>

    @Delete
    suspend fun delete(location: Location)

    @Update
    suspend fun update(location: Location)
}
