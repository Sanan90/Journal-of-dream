// Файл: com/example/journalofdream/model/DreamWithLocations.kt

package com.example.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DreamWithLocations(
    @Embedded val dream: Dream,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DreamLocationCrossRef::class,
            parentColumn = "dreamId",
            entityColumn = "locationId"
        )
    )
    val locations: List<Location>
)
