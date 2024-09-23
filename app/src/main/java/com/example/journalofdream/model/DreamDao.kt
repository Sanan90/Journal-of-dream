package com.example.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.DreamWithLocations
import com.example.journalofdream.model.DreamLocationCrossRef

@Dao
interface DreamDao {

    // Insert a new dream and return its ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dream: Dream): Long



    // Insert a cross-reference between a dream and a location
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDreamLocationCrossRef(crossRef: DreamLocationCrossRef)

    // Update an existing dream
    @Update
    suspend fun update(dream: Dream)

    // Delete a dream
    @Delete
    suspend fun delete(dream: Dream)

    // Get all dreams
    @Query("SELECT * FROM dreams")
    fun getAllDreams(): LiveData<List<Dream>>

    // Get all dreams with their associated locations
    @Transaction
    @Query("SELECT * FROM dreams")
    fun getAllDreamsWithLocations(): LiveData<List<DreamWithLocations>>

    // Get a specific dream with its associated locations by ID
    @Transaction
    @Query("SELECT * FROM dreams WHERE id = :dreamId")
    fun getDreamWithLocationsById(dreamId: Int): LiveData<DreamWithLocations>

    // Delete all cross-references for a specific dream
    @Query("DELETE FROM DreamLocationCrossRef WHERE dreamId = :dreamId")
    suspend fun deleteDreamLocationCrossRefs(dreamId: Int)

    // **Add this method to get dreams associated with a specific location**
    @Transaction
    @Query("""
        SELECT D.* FROM dreams D
        INNER JOIN DreamLocationCrossRef DLCR ON D.id = DLCR.dreamId
        WHERE DLCR.locationId = :locationId
    """)
    fun getDreamsByLocation(locationId: Int): LiveData<List<Dream>>
}
