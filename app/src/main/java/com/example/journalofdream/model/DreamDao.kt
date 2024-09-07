package com.example.journalofdream.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dream: Dream)

    @Query("SELECT * FROM dreams ORDER BY date DESC")
    fun getAllDreams(): LiveData<List<Dream>>

    @Delete
    suspend fun delete(dream: Dream)

    @Update
    suspend fun update(dream: Dream)

    // Новый метод для получения снов по ID локации
    @Query("SELECT * FROM dreams WHERE locationId = :locationId ORDER BY date DESC")
    fun getDreamsByLocation(locationId: Int): LiveData<List<Dream>>
}

