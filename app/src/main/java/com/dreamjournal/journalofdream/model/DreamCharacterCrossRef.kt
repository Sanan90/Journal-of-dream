package com.dreamjournal.journalofdream.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "dream_character_cross_ref",
    primaryKeys = ["dreamId", "characterId"],
    foreignKeys = [
        ForeignKey(
            entity = Dream::class,
            parentColumns = ["localId"],
            childColumns = ["dreamId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = DreamCharacter::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = CASCADE
        )
    ]
)
data class DreamCharacterCrossRef(
    val dreamId: Int,
    val characterId: Int
)
