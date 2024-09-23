package com.example.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DreamWithLocations(
    @Embedded val dream: Dream,
    @Relation(
        parentColumn = "id", // Колонка из таблицы Dream
        entityColumn = "id", // Колонка из таблицы Location
        associateBy = Junction(
            value = DreamLocationCrossRef::class,
            parentColumn = "dreamId", // Колонка из DreamLocationCrossRef, связанная с Dream
            entityColumn = "locationId" // Колонка из DreamLocationCrossRef, связанная с Location
        )
    )
    val locations: List<Location>
)
