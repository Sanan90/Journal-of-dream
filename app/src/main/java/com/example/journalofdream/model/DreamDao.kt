// Файл: com/example/journalofdream/database/DreamDao.kt

package com.example.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.DreamWithLocations
import com.example.journalofdream.model.DreamLocationCrossRef

@Dao
interface DreamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dream: Dream)

    // Вставка связи между сном и локацией
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDreamLocationCrossRef(crossRef: DreamLocationCrossRef)

    @Update
    suspend fun update(dream: Dream)

    @Delete
    suspend fun delete(dream: Dream)

    // Получение всех снов
    @Query("SELECT * FROM dreams")
    fun getAllDreams(): LiveData<List<Dream>>

    // Получение всех снов с их локациями
    @Transaction
    @Query("SELECT * FROM dreams")
    fun getAllDreamsWithLocations(): LiveData<List<DreamWithLocations>>

    // Получение конкретного сна с его локациями по ID
    @Transaction
    @Query("SELECT * FROM dreams WHERE id = :dreamId")
    fun getDreamWithLocationsById(dreamId: String): LiveData<DreamWithLocations>

    // Удаление всех связей для конкретного сна
    @Query("DELETE FROM DreamLocationCrossRef WHERE dreamId = :dreamId")
    suspend fun deleteDreamLocationCrossRefs(dreamId: String)

    // Метод для получения всех снов без LiveData
    @Query("SELECT * FROM dreams")
    suspend fun getAllDreamsOnce(): List<Dream>

    // Метод для получения снов, связанных с конкретной локацией
    @Transaction
    @Query("""
        SELECT D.* FROM dreams D
        INNER JOIN DreamLocationCrossRef DLCR ON D.id = DLCR.dreamId
        WHERE DLCR.locationId = :locationId
    """)
    fun getDreamsByLocation(locationId: Int): LiveData<List<Dream>>


    @Query("SELECT * FROM dreams WHERE title LIKE :query OR content LIKE :query")
    fun searchDreams(query: String): LiveData<List<Dream>>

}
