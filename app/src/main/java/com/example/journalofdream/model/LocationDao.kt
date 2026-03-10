package com.example.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.journalofdream.model.Location
import com.example.journalofdream.model.LocationWithDreams

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location): Long

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    // Список локаций для заданного владельца (LiveData для наблюдения)
    @Query("SELECT * FROM locations WHERE ownerUid = :ownerUid")
    fun getLocationsByOwner(ownerUid: String): LiveData<List<Location>>

    // Получение локации по ID и владельцу (LiveData)
    @Query("SELECT * FROM locations WHERE id = :locationId AND ownerUid = :ownerUid")
    fun getLocationById(locationId: Int, ownerUid: String): LiveData<Location>

    // Локация с привязанными снами (отношение один-ко-многим, LiveData)
    @Transaction
    @Query("SELECT * FROM locations WHERE id = :locationId AND ownerUid = :ownerUid")
    fun getLocationWithDreams(locationId: Int, ownerUid: String): LiveData<LocationWithDreams>

    // Разовое получение всех локаций пользователя (для миграции)
    @Query("SELECT * FROM locations WHERE ownerUid = :ownerUid")
    suspend fun getLocationsByOwnerOnce(ownerUid: String): List<Location>

    // Разовое получение локации по ID и владельцу (для внутренних нужд, например, синхронизации)
    @Query("SELECT * FROM locations WHERE id = :locId AND ownerUid = :ownerUid LIMIT 1")
    suspend fun getLocationByIdOnce(locId: Int, ownerUid: String): Location?

    // Все локации с привязанными снами (для счётчика и сортировки)
    @Transaction
    @Query("SELECT * FROM locations WHERE ownerUid = :ownerUid")
    fun getAllLocationsWithDreams(ownerUid: String): LiveData<List<LocationWithDreams>>

    @Query("DELETE FROM locations WHERE ownerUid = :ownerUid")
    suspend fun deleteAllLocationsByOwner(ownerUid: String)
}
