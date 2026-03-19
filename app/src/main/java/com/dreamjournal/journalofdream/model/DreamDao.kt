package com.dreamjournal.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.model.DreamCharacterCrossRef
import com.dreamjournal.journalofdream.model.DreamLocationCrossRef
import com.dreamjournal.journalofdream.model.DreamWithLocations

@Dao
interface DreamDao {


    // Вставка/обновление сна в локальную БД
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dream: Dream): Long

    @Update
    suspend fun update(dream: Dream)

    @Delete
    suspend fun delete(dream: Dream)

    // Связи сон-локация (вставка и удаление)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDreamLocationCrossRef(ref: DreamLocationCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDreamCharacterCrossRef(ref: DreamCharacterCrossRef)

    @Query("DELETE FROM dream_location_cross_ref WHERE dreamId = :dreamId")
    suspend fun deleteDreamLocationCrossRefs(dreamId: Int)

    @Query("DELETE FROM dream_character_cross_ref WHERE dreamId = :dreamId")
    suspend fun deleteDreamCharacterCrossRefs(dreamId: Int)

    // Получение всех снов для заданного владельца (обновляется в реальном времени)
    @Query("SELECT * FROM dreams WHERE ownerUid = :ownerUid ORDER BY date DESC")
    fun getDreamsByOwner(ownerUid: String): LiveData<List<Dream>>

    // Получение сна по ID вместе с локациями (LiveData для наблюдения)
    @Transaction
    @Query("SELECT * FROM dreams WHERE localId = :dreamId")
    fun getDreamWithLocationsById(dreamId: Int): LiveData<DreamWithLocations>

    // Поиск снов по подстроке (фильтруется по текущему владельцу)
    @Query("SELECT * FROM dreams WHERE (title LIKE :query OR content LIKE :query) AND ownerUid = :ownerUid")
    fun searchDreams(query: String, ownerUid: String): LiveData<List<Dream>>

    // Все сны конкретного владельца (разово, без LiveData) – для миграции данных
    @Query("SELECT * FROM dreams WHERE ownerUid = :ownerUid")
    suspend fun getDreamsByOwnerOnce(ownerUid: String): List<Dream>

    // Получение всех ID локаций, связанных с данным сном (разово)
    @Query("SELECT locationId FROM dream_location_cross_ref WHERE dreamId = :dreamId")
    suspend fun getLocationIdsForDream(dreamId: Int): List<Int>

    @Query("SELECT characterId FROM dream_character_cross_ref WHERE dreamId = :dreamId")
    suspend fun getCharacterIdsForDream(dreamId: Int): List<Int>

    @Query("""
    SELECT d.* FROM dreams d
    INNER JOIN dream_location_cross_ref r ON d.localId = r.dreamId
    WHERE r.locationId = :locationId AND d.ownerUid = :ownerUid
""")
    fun getDreamsByLocation(locationId: Int, ownerUid: String): LiveData<List<Dream>>

    // Получение сна по ID и ownerUid (если нужно разово получить конкретный сон)
    @Query("SELECT * FROM dreams WHERE localId = :dreamId AND ownerUid = :ownerUid")
    suspend fun getDreamByIdOnce(dreamId: Int, ownerUid: String): Dream?

    @Query("DELETE FROM dreams WHERE ownerUid = :ownerUid")
    suspend fun deleteAllDreamsByOwner(ownerUid: String)


}
