package com.dreamjournal.journalofdream.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dreamjournal.journalofdream.model.CharacterWithDreams
import com.dreamjournal.journalofdream.model.DreamCharacter

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: DreamCharacter): Long

    @Update
    suspend fun update(character: DreamCharacter)

    @Delete
    suspend fun delete(character: DreamCharacter)

    @Query("SELECT * FROM characters WHERE ownerUid = :ownerUid ORDER BY name COLLATE NOCASE ASC")
    fun getCharactersByOwner(ownerUid: String): LiveData<List<DreamCharacter>>

    @Query("SELECT * FROM characters WHERE id = :characterId AND ownerUid = :ownerUid")
    fun getCharacterById(characterId: Int, ownerUid: String): LiveData<DreamCharacter>

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :characterId AND ownerUid = :ownerUid")
    fun getCharacterWithDreams(characterId: Int, ownerUid: String): LiveData<CharacterWithDreams>

    @Query("SELECT * FROM characters WHERE ownerUid = :ownerUid")
    suspend fun getCharactersByOwnerOnce(ownerUid: String): List<DreamCharacter>

    @Query("SELECT * FROM characters WHERE id = :characterId AND ownerUid = :ownerUid LIMIT 1")
    suspend fun getCharacterByIdOnce(characterId: Int, ownerUid: String): DreamCharacter?

    @Transaction
    @Query("SELECT * FROM characters WHERE ownerUid = :ownerUid")
    fun getAllCharactersWithDreams(ownerUid: String): LiveData<List<CharacterWithDreams>>
}
