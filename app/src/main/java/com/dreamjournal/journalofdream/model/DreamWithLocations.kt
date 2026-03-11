package com.dreamjournal.journalofdream.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * "Сон с локациями" (одна сторона связи).
 *
 * parentColumn = "localId" (у Dream)
 * entityColumn = "id" (у Location)
 * junction: dreamId -> localId, locationId -> id
 */
data class DreamWithLocations(
    @Embedded val dream: Dream,
    @Relation(
        parentColumn = "localId",   // PK у Dream
        entityColumn = "id",        // PK у Location
        associateBy = Junction(
            DreamLocationCrossRef::class,
            parentColumn = "dreamId",    // поле во DreamLocationCrossRef, указывающее на Dream
            entityColumn = "locationId"  // поле во DreamLocationCrossRef, указывающее на Location
        )
    )
    val locations: List<Location>
)
