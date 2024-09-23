package com.example.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class LocationWithDreams(
    @Embedded val location: Location,
    @Relation(
        parentColumn = "id", // Колонка из таблицы Location
        entityColumn = "id", // Колонка из таблицы Dream
        associateBy = Junction(
            value = DreamLocationCrossRef::class,
            parentColumn = "locationId", // Колонка из DreamLocationCrossRef, связанная с Location
            entityColumn = "dreamId" // Колонка из DreamLocationCrossRef, связанная с Dream
        )
    )
    val dreams: List<Dream>
)
