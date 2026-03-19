package com.dreamjournal.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CharacterWithDreams(
    @Embedded val character: DreamCharacter,
    @Relation(
        parentColumn = "id",
        entityColumn = "localId",
        associateBy = Junction(
            DreamCharacterCrossRef::class,
            parentColumn = "characterId",
            entityColumn = "dreamId"
        )
    )
    val dreams: List<Dream>
)
